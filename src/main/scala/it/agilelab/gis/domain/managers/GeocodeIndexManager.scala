package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.Geometry
import it.agilelab.gis.core.utils.GeocodeManagerUtils.{ BoundaryPathGroup, CountryPathSet, Path }
import it.agilelab.gis.core.utils.{ Configuration, Logger, ObjectPickler }
import it.agilelab.gis.domain.configuration.{ GeoRelationIndexManagerConfiguration, GeocodeIndexManagerConfiguration }
import it.agilelab.gis.domain.loader.{
  OSMAdministrativeBoundariesLoader,
  OSMGenericStreetLoader,
  OSMHouseNumbersLoader,
  OSMPostalCodeLoader
}
import it.agilelab.gis.domain.models.{ OSMBoundary, OSMHouseNumber, OSMStreetAndHouseNumber }
import it.agilelab.gis.domain.spatialList.GeometryList
import it.agilelab.gis.utils.ScalaUtils.{ load, recordDuration }

import java.io.File
import java.util.concurrent.{ Callable, Executors }
import scala.reflect.ClassTag

/** [[GeocodeIndexManager]] creates OSM indices, see [[GeocodeIndexSet]] for a full list of indices created.
  *
  * @param conf OSM maps configurations.
  */
case class GeocodeIndexManager(conf: Config) extends Configuration with Logger {

  val indexConfig: GeocodeIndexManagerConfiguration = GeocodeIndexManagerConfiguration(conf)
  val pathManager: GeocodePathManager = GeocodePathManager(indexConfig.pathConf)
  val boundariesLoader: OSMAdministrativeBoundariesLoader =
    OSMAdministrativeBoundariesLoader(indexConfig.boundaryConf, pathManager)
  val postalCodeLoader: OSMPostalCodeLoader = OSMPostalCodeLoader()
  val indexSet: GeocodeIndexSet = createIndexSet(indexConfig)

  /** [[makeIndices]] creates indices, see [[GeocodeIndexSet]] for the full list of indices created.
    *
    * @param upperFolderPath OSM index input path.
    * @param outputPaths     serialized indices output path.
    */
  def makeIndices(upperFolderPath: String, outputPaths: Option[List[String]]): GeocodeIndexSet = {

    // Check whether the input folder is a directory
    val mapsFolder = new File(upperFolderPath)
    if (!mapsFolder.exists()) {
      throw new IllegalArgumentException(s"$upperFolderPath doesn't exist")
    }
    if (!mapsFolder.isDirectory) {
      throw new IllegalArgumentException(s"$upperFolderPath is not a directory")
    }

    val multiCountriesPathSet: List[CountryPathSet] =
      mapsFolder
        .listFiles()
        .map(pathManager.getCountryPathSet)
        .toList

    // If an output path is defined, we create indices and serialize them in the specified output directory.
    val maybeIndex: Option[GeocodeIndexSet] = if (outputPaths.isDefined) {
      outputPaths.get match {
        case bPath :: sPath :: hPath :: Nil =>
          logger.info(s"Saving OSM index in $bPath $sPath $hPath")
          makeIndex[OSMBoundary](bPath, createBoundariesGeometryList(multiCountriesPathSet))
          makeIndex[OSMStreetAndHouseNumber](sPath, createStreetsGeometryList(multiCountriesPathSet))
          makeIndex[OSMHouseNumber](hPath, createHouseNumbersGeometryList(multiCountriesPathSet))

          Some(
            createIndexSet(
              indexConfig.copy(
                isSerializedInputPaths = true,
                inputPaths = outputPaths.get
              )))
        case _ =>
          logger.warn(s"Index serialization skipped due to unexpected output paths $outputPaths")
          None
      }
    } else {
      None
    }

    if (maybeIndex.isDefined) {
      maybeIndex.get
    } else {
      logger.info("Creating in memory indices ...")
      val indexSet =
        recordDuration(createIndexSet(multiCountriesPathSet), d => logger.info(s"Created in memory indices in $d ms"))
      indexSet
    }
  }

  /** Loads and creates an index set from the given input directories.
    *
    * @param multiCountriesPathSet input paths
    * @return index set
    */
  private def createIndexSet(multiCountriesPathSet: List[CountryPathSet]): GeocodeIndexSet = {
    val boundariesGeometryList: GeometryList[OSMBoundary] = createBoundariesGeometryList(multiCountriesPathSet)
    val streetsGeometryList: GeometryList[OSMStreetAndHouseNumber] = createStreetsGeometryList(multiCountriesPathSet)
    val houseNumbersGeometryList: GeometryList[OSMHouseNumber] = createHouseNumbersGeometryList(multiCountriesPathSet)
    createIndexSet(boundariesGeometryList, streetsGeometryList, houseNumbersGeometryList)
  }

  /** Creates an index set using the given geometry lists.
    *
    * @param boundariesGeometryList   list of boundaries geometry list.
    *                                 Elements:
    *                                - 1: Boundaries geometry list
    *                                - 2: Regions geometry list (Optional)
    *                                  Constraint: 1 <= size <= 2.
    * @param streetsGeometryList      geometry list of streets.
    * @param houseNumbersGeometryList geometry list for house numbers
    * @return index set
    */
  private def createIndexSet(
      boundariesGeometryList: GeometryList[OSMBoundary],
      streetsGeometryList: GeometryList[OSMStreetAndHouseNumber],
      houseNumbersGeometryList: GeometryList[OSMHouseNumber]
  ): GeocodeIndexSet = GeocodeIndexSet(boundariesGeometryList, streetsGeometryList, houseNumbersGeometryList)

  private def createHouseNumbersGeometryList(multiCountriesPathSet: List[CountryPathSet]) = {
    logger.info("Loading OSM house numbers file into GeometryList...")
    val houseNumbers: List[Path] = multiCountriesPathSet.flatMap(_.houseNumbers)
    val houseNumbersGeometryList: GeometryList[OSMHouseNumber] = createHouseNumbersIndex(houseNumbers)
    logger.info("Done loading OSM house numbers file into GeometryList!")
    houseNumbersGeometryList
  }

  private def createStreetsGeometryList(multiCountriesPathSet: List[CountryPathSet]) = {
    logger.info("Loading OSM roads file into GeometryList...")
    val roads: List[Path] = multiCountriesPathSet.flatMap(_.roads)
    val streetsGeometryList: GeometryList[OSMStreetAndHouseNumber] = createAddressesIndex(roads)
    logger.info("Done loading OSM roads file into GeometryList!")
    streetsGeometryList
  }

  /** createBoundariesGeometryList creates a list of geometry list.
    *
    * @param multiCountriesPathSet country path set, see [[GeocodePathManager.getCountryPathSet()]]
    * @return
    */
  private def createBoundariesGeometryList(
      multiCountriesPathSet: List[CountryPathSet]
  ): GeometryList[OSMBoundary] = {
    logger.info("Loading OSM boundaries file into GeometryList...")

    val boundariesIndices: List[BoundaryIndices] =
      multiCountriesPathSet.par
        .map(pathSet => createCountryBoundariesWithPostalCodes(pathSet.boundary, pathSet.postalCodes))
        .toList

    val boundariesGeometryList = boundariesLoader.buildIndex(boundariesIndices.flatMap(_.index))

    logger.info("Done loading OSM boundaries file into GeometryList!")

    boundariesGeometryList
  }

  //TODO review performances
  def createCountryBoundariesWithPostalCodes(
      paths: BoundaryPathGroup,
      postalCodesPath: Array[Path]
  ): BoundaryIndices = {

    val loadPostalCode: Seq[Path] => Seq[OSMBoundary] = pathList => pathList.flatMap(postalCodeLoader.loadObjects(_))
    val loadBoundaries: Seq[Path] => Seq[OSMBoundary] = pathList => pathList.flatMap(boundariesLoader.loadObjects(_))

    val countryBoundary: Seq[OSMBoundary] = loadBoundaries(paths.country)

    val countryName = countryBoundary.head.country.getOrElse("UNDEFINED COUNTRY")

    logger.info(s"Start loading boundary of: $countryName...")

    val boundaries: Seq[Seq[OSMBoundary]] = Seq(
      // Note the order is important, start from the biggest area
      countryBoundary,
      loadBoundaries(paths.region),
      loadBoundaries(paths.county),
      loadBoundaries(paths.city),
      loadPostalCode(postalCodesPath)
    )

    val boundariesEnriched = boundaries
      .reduce((a, b) => mergeBoundaries(b, a))

    logger.info(s"Loaded boundary of: $countryName...")

    BoundaryIndices(boundariesEnriched)
  }

  /** Merges the inner boundaries with the additional attributes of the matching outers.
    * If an element of the inner boundaries is contained inside an element of the outer boundaries, the inner boundary
    * is merged with the outer one. The merge will add to the inner boundary all the attributes of the outer one that
    * are not defined yet.
    *
    * @param inner collection of boundaries that could be contained in some outer ones
    * @param outer collection of boundaries that could contain some elements of the inner ones
    * @return the merged boundaries (if inner is empty returns outer)
    */
  protected def mergeBoundaries(inner: Seq[OSMBoundary], outer: Seq[OSMBoundary]): Seq[OSMBoundary] = {
    val innerPar = inner.par
    outer.par
      .map(b => (b, innerPar.filter(_.multiPolygon.getInteriorPoint.coveredBy(b))))
      .flatMap { case (out, inners) =>
        if (inners.isEmpty)
          Seq(out)
        else
          inners.map(_.merge(out))
      }
      .seq
  }

  /** Create the addresses index that will be used to decorate the road index leaves by adding
    * a sequence of [[OSMStreetAndHouseNumber]] to retrieve the candidate street number
    */
  def createAddressesIndex(roads: Seq[Path]): GeometryList[OSMStreetAndHouseNumber] = {
    logger.info(s"Loading OSM roads ...")
    recordDuration(
      OSMGenericStreetLoader(roads, Seq()).loadIndex(roads: _*),
      d => logger.info(s"Done loading OSM roads in $d ms!")
    )
  }

  def createHouseNumbersIndex(houseNumbers: List[Path]): GeometryList[OSMHouseNumber] = {
    logger.info(s"Loading OSM house numbers ...")
    recordDuration(
      new OSMHouseNumbersLoader().loadIndex(houseNumbers: _*),
      d => logger.info(s"Done loading OSM house numbers in $d ms!")
    )
  }

  /** Creates indices and serializes them in the configured output directory if specified.
    *
    * @param config indices configuration
    * @return a set of indices we care about.
    */
  private def createIndexSet(config: GeocodeIndexManagerConfiguration): GeocodeIndexSet = {
    val inputPaths = config.inputPaths
    if (config.isSerializedInputPaths) {
      inputPaths match {
        case bPath :: sPath :: hPath :: Nil =>
          logger.info(s"Loading index from files $bPath $sPath $hPath")

          val threadPool = Executors.newFixedThreadPool(inputPaths.size)
          val boundaries = threadPool.submit(createIndex[OSMBoundary](bPath))
          val streets = threadPool.submit(createIndex[OSMStreetAndHouseNumber](sPath))
          val houseNumbers = threadPool.submit(createIndex[OSMHouseNumber](hPath))

          val index = createIndexSet(boundaries.get(), streets.get(), houseNumbers.get())
          threadPool.shutdown()
          index
        case _ =>
          throw new IllegalArgumentException(
            s"the list of input paths should be three " +
              s"different paths (boundaries, streets, and house numbers). The list of input path was: $inputPaths")
      }
    } else {
      inputPaths match {
        case path :: Nil => makeIndices(path, config.outputPaths)
        case _ =>
          throw new IllegalArgumentException(
            s"the list of input paths should be a single path. " +
              s"The list of input path was: $inputPaths")
      }
    }
  }

  /** @param path the path in which the index will be stored
    * @param geometryList the [[GeometryList]] saved at the given path
    * @tparam T the specific [[Geometry]] class
    */
  protected def makeIndex[T <: Geometry](path: Path, geometryList: GeometryList[T])(implicit
      ctag: ClassTag[T]
  ): Unit = {
    recordDuration(
      ObjectPickler.pickle(geometryList, path),
      d => logger.info(s"Saved index for ${ctag.runtimeClass.getSimpleName} to file $path in $d ms")
    )
    System.gc()
  }

  /** Loads the index for a particular geometry [[T]]
    *
    * @param path the path in which the index is saved
    * @tparam T the specific [[Geometry]] class
    * @return a [[Callable]] version of [[GeometryList]]
    */
  protected def createIndex[T <: Geometry](path: Path)(implicit ctag: ClassTag[T]): Callable[GeometryList[T]] =
    load(
      recordDuration(
        ObjectPickler.unpickle[GeometryList[T]](path),
        d => {
          logger.info(s"Loaded index for ${ctag.runtimeClass.getSimpleName} from file $path in $d ms")
          System.gc()
        }
      )
    )
}
