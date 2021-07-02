package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ManagerUtils.{ BoundaryPathGroup, CountryPathSet, Path }
import it.agilelab.gis.core.utils.{ Configuration, Logger, ObjectPickler }
import it.agilelab.gis.domain.configuration.IndexManagerConfiguration
import it.agilelab.gis.domain.loader.{
  OSMAdministrativeBoundariesLoader,
  OSMGenericStreetLoader,
  OSMHouseNumbersLoader,
  OSMPostalCodeLoader
}
import it.agilelab.gis.domain.managers.IndexManager._
import it.agilelab.gis.domain.models.{ OSMBoundary, OSMHouseNumber, OSMStreetAndHouseNumber }
import it.agilelab.gis.domain.spatialList.GeometryList

import java.io.File
import java.util.concurrent.{ Callable, Executors, Future }

/** [[IndexManager]] creates OSM indices, see [[IndexSet]] for a full list of indices created.
  *
  * @param conf OSM maps configurations.
  */
case class IndexManager(conf: Config) extends Configuration with Logger {

  val indexConfig: IndexManagerConfiguration = IndexManagerConfiguration(conf)
  val pathManager: PathManager = PathManager(indexConfig.pathConf)
  val boundariesLoader: OSMAdministrativeBoundariesLoader =
    OSMAdministrativeBoundariesLoader(indexConfig.boundaryConf, pathManager)
  val postalCodeLoader: OSMPostalCodeLoader = OSMPostalCodeLoader()
  val indexSet: IndexSet = createIndexSet(indexConfig)

  /** [[makeIndices()]] creates indices, see [[IndexSet]] for the full list of indices created.
    *
    * @param upperFolderPath OSM index input path.
    * @param outputPaths     serialized indices output path.
    */
  def makeIndices(upperFolderPath: String, outputPaths: Option[List[String]]): IndexSet = {

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
    val maybeIndex: Option[IndexSet] = if (outputPaths.isDefined) {
      outputPaths.get match {
        case bPath :: sPath :: hPath :: Nil =>
          logger.info(s"Saving OSM index in $bPath $sPath $hPath")
          recordDuration(
            ObjectPickler.pickle(createBoundariesGeometryList(multiCountriesPathSet), bPath),
            d => logger.info(s"Saved OSM boundaries index in $d ms"))
          System.gc()
          recordDuration(
            ObjectPickler.pickle(createStreetsGeometryList(multiCountriesPathSet), sPath),
            d => logger.info(s"Saved OSM streets index in $d ms"))
          System.gc()
          recordDuration(
            ObjectPickler.pickle(createHouseNumbersGeometryList(multiCountriesPathSet), hPath),
            d => logger.info(s"Saved OSM house numbers index in $d ms"))
          System.gc()
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
  private def createIndexSet(multiCountriesPathSet: List[CountryPathSet]): IndexSet = {
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
  ): IndexSet = IndexSet(boundariesGeometryList, streetsGeometryList, houseNumbersGeometryList)

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
    * @param multiCountriesPathSet country path set, see [[PathManager.getCountryPathSet()]]
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

    /*
      TODO inner points seem invalid, what's going on?
                                                                         lon               lat
        cities      = outer.head.multiPolygon.getInteriorPoint => POINT (10.552518190637718 44.0691814)
        postalCodes = inner.head.multiPolygon.getInteriorPoint => POINT (506157.0602102479 5043965.77465)

        result = {ArrayBuffer@5303} "ArrayBuffer" size = 70
          0 = {Point@5311} "POINT (506157.0602102479 5043965.77465)"
          1 = {Point@5312} "POINT (510661.48970780906 5027298.6375)"
          2 = {Point@5313} "POINT (509253.14447797695 5044043.45465)"
          3 = {Point@5314} "POINT (515279.61794179614 5042582.7918)"
          4 = {Point@5315} "POINT (507962.20471614157 5028183.7756)"
          5 = {Point@5316} "POINT (525696.9406875303 5041216.138049999)"
          6 = {Point@5317} "POINT (506610.817870317 5031877.981699999)"
          7 = {Point@5318} "POINT (517308.3037883795 5044923.3567)"
          8 = {Point@5319} "POINT (522388.07241711474 5042256.6203000005)"
          9 = {Point@5320} "POINT (512552.6081004639 5043443.727750001)"
          10 = {Point@5321} "POINT (508145.7095425037 5031212.34355)"
          11 = {Point@5322} "POINT (514331.3952902251 5044327.600099999)"
          12 = {Point@5323} "POINT (503151.01041641843 5027019.6633)"
          13 = {Point@5324} "POINT (525955.4610106011 5028645.8995)"
          14 = {Point@5325} "POINT (514655.9505141515 5035495.42695)"
          15 = {Point@5326} "POINT (515498.9882354425 5034397.15905)"
          16 = {Point@5327} "POINT (513873.39409143676 5034404.0337000005)"
          17 = {Point@5328} "POINT (515166.6693910803 5036853.6107)"
          18 = {Point@5329} "POINT (516237.21109014936 5038229.26185)"
          19 = {Point@5330} "POINT (516816.78124754527 5040147.7852)"
          20 = {Point@5331} "POINT (517667.76235221134 5038270.9388999995)"
          21 = {Point@5332} "POINT (518698.69077912666 5039996.5207)"
          22 = {Point@5333} "POINT (516772.89260896656 5035420.804450001)"
          23 = {Point@5334} "POINT (517714.21014384937 5037079.32945)"
          24 = {Point@5335} "POINT (519379.86097138864 5038852.1374)"
          25 = {Point@5336} "POINT (518052.0022462623 5035604.66675)"
          26 = {Point@5337} "POINT (519657.5437793381 5036615.39235)"
          27 = {Point@5338} "POINT (516332.1886599524 5033377.43195)"
          28 = {Point@5339} "POINT (514650.71622536675 5032759.0341)"
          29 = {Point@5340} "POINT (517701.13172308065 5033405.47605)"
          30 = {Point@5341} "POINT (519642.92669709853 5032144.0003)"
          31 = {Point@5342} "POINT (518315.85437104985 5030536.82575)"
          32 = {Point@5343} "POINT (516310.71146560414 5029549.586)"
          33 = {Point@5344} "POINT (513281.1164557027 5029101.292900001)"
          34 = {Point@5345} "POINT (512390.17910346726 5032347.86675)"
          35 = {Point@5346} "POINT (512812.28023864876 5033629.36245)"
          36 = {Point@5347} "POINT (512664.7785104192 5035654.69575)"
          37 = {Point@5348} "POINT (511205.87627107516 5033565.6195)"
          38 = {Point@5349} "POINT (509706.2696752737 5033429.938250001)"
          39 = {Point@5350} "POINT (510824.1953795857 5036282.76855)"
          40 = {Point@5351} "POINT (511602.83893517987 5036173.4976)"
          41 = {Point@5352} "POINT (509148.9716476591 5037793.05225)"
          42 = {Point@5353} "POINT (508408.15331305977 5033031.618749999)"
          43 = {Point@5354} "POINT (507819.7916726992 5035786.9398)"
          44 = {Point@5355} "POINT (513697.85851219174 5036607.03855)"
          45 = {Point@5356} "POINT (512334.72240252106 5037773.9509499995)"
          46 = {Point@5357} "POINT (511081.37722557964 5038749.973250001)"
          47 = {Point@5358} "POINT (509763.7279579347 5040422.6631000005)"
          48 = {Point@5359} "POINT (513339.73090388865 5038328.79285)"
          49 = {Point@5360} "POINT (514688.6321240104 5038050.6562)"
          50 = {Point@5361} "POINT (513114.88698214456 5040780.75)"
          51 = {Point@5362} "POINT (515274.1281952229 5040397.42545)"
          52 = {Point@5363} "POINT (510919.8259194087 5042072.869299999)"
          53 = {Point@5364} "POINT (513121.91249461623 5046519.28085)"
          54 = {Point@5365} "POINT (506372.31252481975 5039668.56905)"
          55 = {Point@5366} "POINT (523840.26770104945 5032144.0003)"
          56 = {Point@5367} "POINT (525323.0246660481 5037233.808150001)"
          57 = {Point@5368} "POINT (503635.55075945205 5041983.3285)"
          58 = {Point@5369} "POINT (512799.6560544504 5025414.67585)"
          59 = {Point@5370} "POINT (521266.1140521312 5028918.14855)"
          60 = {Point@5371} "POINT (521972.3551090481 5025292.1753)"
          61 = {Point@5372} "POINT (522249.4624991986 5036648.56595)"
          62 = {Point@5373} "POINT (518922.8028288932 5042633.89095)"
          63 = {Point@5374} "POINT (504072.1321000897 5036686.0909)"
          64 = {Point@5375} "POINT (504440.641091837 5029839.892899999)"
          65 = {Point@5376} "POINT (522505.4141800825 5040232.56445)"
          66 = {Point@5377} "POINT (508774.6038332972 5024415.18265)"
          67 = {Point@5378} "POINT (508658.10515817016 5041641.8325000005)"
          68 = {Point@5379} "POINT (523121.65698994533 5044546.7443)"
          69 = {Point@5380} "POINT (521417.4472475906 5048860.17915)"
     */

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
  private def createIndexSet(config: IndexManagerConfiguration): IndexSet = {
    val inputPaths = config.inputPaths
    if (config.isSerializedInputPaths) {
      inputPaths match {
        case bPath :: sPath :: hPath :: Nil =>
          logger.info(s"Loading index from files $bPath $sPath $hPath")

          val threadPool = Executors.newFixedThreadPool(inputPaths.size)
          val boundaries: Future[GeometryList[OSMBoundary]] = threadPool.submit(
            load(
              recordDuration(
                ObjectPickler.unpickle[GeometryList[OSMBoundary]](bPath),
                d => {
                  logger.info(s"Loaded boundaries index from file $bPath in $d ms")
                  System.gc()
                })
            ))
          val streets = threadPool.submit(
            load(
              recordDuration(
                ObjectPickler.unpickle[GeometryList[OSMStreetAndHouseNumber]](sPath),
                d => {
                  logger.info(s"Loaded streets index from file $sPath in $d ms")
                  System.gc()
                })
            ))
          val houseNumbers = threadPool.submit(
            load(
              recordDuration(
                ObjectPickler.unpickle[GeometryList[OSMHouseNumber]](hPath),
                d => {
                  logger.info(s"Loaded house numbers index from file $hPath in $d ms")
                  System.gc()
                })
            ))

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
}

object IndexManager {

  private def recordDuration[T](f: => T, duration: Long => Unit): T = {
    val start = System.currentTimeMillis()
    val r = f
    duration(System.currentTimeMillis() - start)
    r
  }

  private def load[T](f: => T): Callable[T] = new Callable[T] {
    override def call(): T = f
  }
}
