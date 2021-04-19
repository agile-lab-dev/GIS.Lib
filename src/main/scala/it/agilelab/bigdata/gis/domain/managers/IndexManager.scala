package it.agilelab.bigdata.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.ManagerUtils.{BoundaryPathGroup, CountryPathSet, Path}
import it.agilelab.bigdata.gis.core.utils.{Configuration, Logger, ObjectPickler}
import it.agilelab.bigdata.gis.domain.configuration.IndexManagerConfiguration
import it.agilelab.bigdata.gis.domain.loader.{OSMAdministrativeBoundariesLoader, OSMGenericStreetLoader, OSMHouseNumbersLoader, OSMPostalCodeLoader}
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
  val indexSet: IndexSet = createIndexSet(indexConfig.inputPaths)

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
  def makeIndices(upperFolderPath: String): IndexSet = {

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
    val regionIndexStuff: List[OSMBoundary] = indexStuffs.flatMap(_.regionIndex)

    val boundariesGeometryList: GeometryList[OSMBoundary] = boundariesLoader.buildIndex(cityIndexStuff)

    val regionGeometryList: GeometryList[OSMBoundary] = if (regionIndexStuff.nonEmpty)
        boundariesLoader.buildIndex(regionIndexStuff)
      else
        null

    logger.info("Done loading OSM boundaries file into GeometryList!")

    val roads: List[Path] = multiCountriesPathSet.flatMap(_.roads)
    val houseNumbers: List[Path] = multiCountriesPathSet.flatMap(_.houseNumbers)

    val streetsGeometryList: GeometryList[OSMStreetAndHouseNumber] = createAddressesIndex(roads)

    val houseNumbersGeometryList: GeometryList[OSMHouseNumber] = createHouseNumbersIndex(houseNumbers)

    //trigger garbage collector to remove the addressNumberGeometryList if still in memory
    System.gc()

    IndexSet(boundariesGeometryList, regionGeometryList, streetsGeometryList, houseNumbersGeometryList)
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

    val postalCodesWithCities: Seq[OSMBoundary] = enrichCities(cities, postalCodes)
    val citiesWithCounties: Seq[OSMBoundary] = mergeBoundaries(postalCodesWithCities, counties)
    val countiesWithRegion: Seq[OSMBoundary] = mergeBoundaries(citiesWithCounties, regions)

    val primaryIndexBoundaries: Seq[OSMBoundary] = countiesWithRegion.map(_.merge(countryBoundary))
    val secondaryIndexBoundaries: Seq[OSMBoundary] = regions.map(_.merge(countryBoundary))

    logger.info(s"$countryName loaded.")

    IndexStuffs(secondaryIndexBoundaries, primaryIndexBoundaries)
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
      inner.map { boundary =>
        outer
          .filter(_.customCovers(boundary))
          .find(county => boundary.multiPolygon.getInteriorPoint.coveredBy(county.multiPolygon))
          .map(boundary.merge)
          .getOrElse(boundary)
      }
    }

  /**
   * Enrich when possible all cities with a postalCode.
   * This is done checking if a postalCode is contained into the boundaries of a city
   *
   * @param cities list of cities polygons
   * @param postalCodes list of postalCode points
   * @return same sequence of cities, but enriched when possible with postalCodes
   */
  private def enrichCities(cities: Seq[OSMBoundary], postalCodes: Seq[OSMPostalCode]): Seq[OSMBoundary] = {

    cities
      .filter(_.city.isDefined)
      .map{ city =>
        postalCodes.find(_.point.coveredBy(city.multiPolygon)) match {
          case Some(found) => city.copy(postalCode = found.postalCode)
          case _ => city
       }
     }
  }

  /** Create the addresses index that will be used to decorate the road index leaves by adding
   * a sequence of [[OSMStreetAndHouseNumber]] to retrieve the candidate street number
   */
  def createAddressesIndex(roads: Seq[Path]): GeometryList[OSMStreetAndHouseNumber] = {

    val countryName = roads.head.split(Pattern.quote(File.separator)).reverse.tail.head
    logger.info(s"Loading OSM roads of $countryName...")
    val roadsGeometryList = OSMGenericStreetLoader(roads, Seq()).loadIndex(roads: _*)
    logger.info(s"Done loading OSM roads of $countryName!")

    roadsGeometryList
  }

  def createHouseNumbersIndex(houseNumbers: List[Path]): GeometryList[OSMHouseNumber] = {

    val countryName = houseNumbers.head.split(Pattern.quote(File.separator)).reverse.tail.head
    logger.info(s"Loading OSM house numbers of $countryName...")
    val houseNumbersGeometryList = new OSMHouseNumbersLoader().loadIndex(houseNumbers: _*)
    logger.info(s"Done loading OSM house numbers of $countryName!")

    houseNumbersGeometryList
  }

  private def createIndexSet(inputPaths: List[String]): IndexSet = {
    inputPaths match {
      case path :: Nil => makeIndices(path)
      case bPath :: rPath :: sPath :: Nil =>
        val boundaries = ObjectPickler.unpickle[GeometryList[OSMBoundary]](bPath)
        val regions = ObjectPickler.unpickle[GeometryList[OSMBoundary]](rPath)
        val streets = ObjectPickler.unpickle[GeometryList[OSMStreetAndHouseNumber]](sPath)
        val houseNumbers = ObjectPickler.unpickle[GeometryList[OSMHouseNumber]](sPath)
        IndexSet(boundaries, regions, streets, houseNumbers)
      case _ => throw new IllegalArgumentException(s"the list of input paths should be a single path or three " +
        s"different paths (boundaries, regions and streets). The list of input path was: $inputPaths")
    }
  }
}