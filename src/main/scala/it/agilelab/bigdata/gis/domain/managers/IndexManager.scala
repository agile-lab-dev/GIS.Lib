package it.agilelab.bigdata.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.ManagerUtils.{BoundaryPathGroup, CountryPathSet, Path}
import it.agilelab.bigdata.gis.core.utils.{Configuration, Logger, ObjectPickler}
import it.agilelab.bigdata.gis.domain.configuration.IndexManagerConfiguration
import it.agilelab.bigdata.gis.domain.loader.{OSMAdministrativeBoundariesLoader, OSMGenericStreetLoader, OSMHouseNumbersLoader, OSMPostalCodeLoader}
import it.agilelab.bigdata.gis.domain.managers.IndexManager.recordDuration
import it.agilelab.bigdata.gis.domain.models.{OSMBoundary, OSMHouseNumber, OSMPostalCode, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import java.io.File
import java.util.regex.Pattern

case class IndexManager(conf: Config) extends Configuration with Logger {

  val indexConfig: IndexManagerConfiguration = IndexManagerConfiguration(conf)
  val pathManager: PathManager = PathManager(indexConfig.pathConf)
  val boundariesLoader: OSMAdministrativeBoundariesLoader =
    OSMAdministrativeBoundariesLoader(indexConfig.boundaryConf, pathManager)
  val postalCodeLoader: OSMPostalCodeLoader = OSMPostalCodeLoader()
  val indexSet: IndexSet = createIndexSet(indexConfig)

  /**
   * 2.shp identifica la Nazione [Country]
   * 4.shp identifica la Regione [Regions]
   * 5.shp identifica la YYYY (mutualmente esclusivo con XXX.shp)
   * 6.shp identifica la Provincia [County]
   * 7.shp identifica il Comune [Municipality]
   * 8.shp identifica il Comune (mutualmente esclusivo con 7.shp)
   *
   * @param upperFolderPath path delle mappe
   */
  def makeIndices(upperFolderPath: String, outputPaths: Option[List[String]]): IndexSet = {

    val multiCountriesPathSet: List[CountryPathSet] =
      new File(upperFolderPath)
        .listFiles()
        .map(pathManager.getCountryPathSet)
        .toList

    logger.info("Loading OSM boundaries file into GeometryList...")

    val indexStuffs: List[IndexStuffs] =
      multiCountriesPathSet
        .par
        .map(pathSet => createCountryBoundariesWithPostalCodes(pathSet.boundary, pathSet.postalCodes))
        .toList

    val cityIndexStuff: List[OSMBoundary] = indexStuffs.flatMap(_.cityIndex)

    val boundariesGeometryList: GeometryList[OSMBoundary] = boundariesLoader.buildIndex(cityIndexStuff)

    logger.info("Done loading OSM boundaries file into GeometryList!")

    logger.info("Loading OSM roads file into GeometryList...")
    val roads: List[Path] = multiCountriesPathSet.flatMap(_.roads)
    val streetsGeometryList: GeometryList[OSMStreetAndHouseNumber] = createAddressesIndex(roads)
    logger.info("Done loading OSM roads file into GeometryList!")

    logger.info("Loading OSM house numbers file into GeometryList...")
    val houseNumbers: List[Path] = multiCountriesPathSet.flatMap(_.houseNumbers)
    val houseNumbersGeometryList: GeometryList[OSMHouseNumber] = createHouseNumbersIndex(houseNumbers)
    logger.info("Done loading OSM house numbers file into GeometryList!")

    System.gc()

    if (outputPaths.isDefined) {
      outputPaths.get match {
        case bPath :: sPath :: hPath :: Nil =>
          logger.info(s"Saving OSM index in $bPath $sPath $hPath")
          recordDuration(ObjectPickler.pickle(boundariesGeometryList, bPath), d => {
            logger.info(s"Saved OSM boundaries index in $d ms")
          })
          System.gc()
          recordDuration(ObjectPickler.pickle(streetsGeometryList, sPath), d => {
            logger.info(s"Saved OSM streets index in $d ms")
          })
          System.gc()
          recordDuration(ObjectPickler.pickle(houseNumbersGeometryList, hPath), d => {
            logger.info(s"Saved OSM house numbers index in $d ms")
          })
          System.gc()
        case _ =>
          logger.warn(s"Index serialization skipped due to unexpected output paths $outputPaths")
      }
    }
    IndexSet(boundariesGeometryList, streetsGeometryList, houseNumbersGeometryList)
  }

  //TODO review performances
  def createCountryBoundariesWithPostalCodes(paths: BoundaryPathGroup, postalCodesPath: Array[Path]): IndexStuffs = {

    val loadPostalCode: Seq[Path] => Seq[OSMPostalCode] = pathList => pathList.flatMap(postalCodeLoader.loadObjects(_))
    val loadBoundaries: Seq[Path] => Seq[OSMBoundary] = pathList => pathList.flatMap(boundariesLoader.loadObjects(_))

    val postalCodes: Seq[OSMPostalCode] = loadPostalCode(postalCodesPath)
    val cities: Seq[OSMBoundary] = loadBoundaries(paths.city)
    val counties: Seq[OSMBoundary] = loadBoundaries(paths.county)
    val regions: Seq[OSMBoundary] = loadBoundaries(paths.region)
    val countryBoundary: OSMBoundary = loadBoundaries(paths.country).head

    val countryName = countryBoundary.country.getOrElse("UNDEFINED COUNTRY")

    logger.info(s"Start loading boundary of: $countryName...")

    logger.info(s"Enriching cities of country $countryName ...")
    val postalCodesWithCities: Seq[OSMBoundary] = recordDuration(enrichCities(cities, postalCodes), d => {
      logger.info(s"Done enriching cities of country $countryName in $d ms")
    })

    logger.info(s"Merging boundaries postal codes with cities of country $countryName ...")
    val citiesWithCounties: Seq[OSMBoundary] = recordDuration(mergeBoundaries(postalCodesWithCities, counties), d => {
      logger.info(s"Done merging boundaries postal codes with cities of country $countryName in $d ms")
    })

    logger.info(s"Merging boundaries cities with counties of country $countryName ...")
    val countiesWithRegion: Seq[OSMBoundary] = recordDuration(mergeBoundaries(citiesWithCounties, regions), d => {
      logger.info(s"Done merging boundaries cities with counties of country $countryName in $d ms")
    })

    val primaryIndexBoundaries: Seq[OSMBoundary] = countiesWithRegion.map(_.merge(countryBoundary))

    logger.info(s"$countryName loaded.")

    IndexStuffs(primaryIndexBoundaries)
  }

  /**
   * Merges the inner boundaries with the additional attributes of the matching outers.
   * If an element of the inner boundaries is contained inside an element of the outer boundaries, the inner boundary
   * is merged with the outer one. The merge will add to the inner boundary all the attributes of the outer one that
   * are not defined yet.
   *
   * @param inner collection of boundaries that could be contained in some outer ones
   * @param outer collection of boundaries that could contain some elements of the inner ones
   * @return the merged boundaries (if inner is empty returns outer)
   */
  protected def mergeBoundaries(inner: Seq[OSMBoundary],
                                outer: Seq[OSMBoundary]): Seq[OSMBoundary] =
    if (inner.isEmpty) {
      outer
    } else {
      val outerPar = outer.par
      inner
        .par
        .map { boundary =>
          outerPar
            .filter(_.customCovers(boundary))
            .find(county => boundary.multiPolygon.getInteriorPoint.coveredBy(county.multiPolygon))
            .map(boundary.merge)
            .getOrElse(boundary)
        }.seq
    }

  /**
   * Enrich when possible all cities with a postalCode.
   * This is done checking if a postalCode is contained into the boundaries of a city
   *
   * @param cities      list of cities polygons
   * @param postalCodes list of postalCode points
   * @return same sequence of cities, but enriched when possible with postalCodes
   */
  private def enrichCities(cities: Seq[OSMBoundary], postalCodes: Seq[OSMPostalCode]): Seq[OSMBoundary] = {

    val postalCodesPar = postalCodes.par
    cities
      .par
      .filter(_.city.isDefined)
      .map { city =>
        postalCodesPar.find(_.point.coveredBy(city.multiPolygon)) match {
          case Some(found) => city.copy(postalCode = found.postalCode)
          case _ => city
        }
      }.seq
  }

  /** Create the addresses index that will be used to decorate the road index leaves by adding
   * a sequence of [[OSMStreetAndHouseNumber]] to retrieve the candidate street number
   */
  def createAddressesIndex(roads: Seq[Path]): GeometryList[OSMStreetAndHouseNumber] = {

    val countryName = roads.head.split(Pattern.quote(File.separator)).reverse.tail.head
    logger.info(s"Loading OSM roads of $countryName...")
    val roadsGeometryList = recordDuration(OSMGenericStreetLoader(roads, Seq()).loadIndex(roads: _*), d => {
      logger.info(s"Done loading OSM roads of $countryName in $d ms!")
    })

    roadsGeometryList
  }

  def createHouseNumbersIndex(houseNumbers: List[Path]): GeometryList[OSMHouseNumber] = {

    val countryName = houseNumbers.head.split(Pattern.quote(File.separator)).reverse.tail.head
    logger.info(s"Loading OSM house numbers of $countryName...")
    val houseNumbersGeometryList = recordDuration(new OSMHouseNumbersLoader().loadIndex(houseNumbers: _*), d => {
      logger.info(s"Done loading OSM house numbers of $countryName in $d ms!")
    })

    houseNumbersGeometryList
  }

  private def createIndexSet(config: IndexManagerConfiguration): IndexSet = {
    val inputPaths = config.inputPaths
    if (config.isSerializedInputPaths) {
      inputPaths match {
        case bPath :: sPath :: hPath :: Nil =>
          logger.info(s"Loading index from files $bPath $sPath $hPath")
          val boundaries = recordDuration(ObjectPickler.unpickle[GeometryList[OSMBoundary]](bPath), d => {
            logger.info(s"Loaded boundaries index from file $bPath in $d ms")
          })
          val streets = recordDuration(ObjectPickler.unpickle[GeometryList[OSMStreetAndHouseNumber]](sPath), d => {
            logger.info(s"Loaded streets index from file $sPath in $d ms")
          })
          val houseNumbers = recordDuration(ObjectPickler.unpickle[GeometryList[OSMHouseNumber]](hPath), d => {
            logger.info(s"Loaded house numbers index from file $hPath in $d ms")
          })
          IndexSet(boundaries, streets, houseNumbers)
        case _ => throw new IllegalArgumentException(s"the list of input paths should be three " +
          s"different paths (boundaries, streets, and house numbers). The list of input path was: $inputPaths")
      }
    } else {
      inputPaths match {
        case path :: Nil => makeIndices(path, config.outputPaths)
        case _ => throw new IllegalArgumentException(s"the list of input paths should be a single path. " +
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

}