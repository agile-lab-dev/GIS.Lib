package it.agilelab.bigdata.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.ManagerUtils.BoundariesPos.{CITIES, COUNTIES, COUNTRY, REGIONS}
import it.agilelab.bigdata.gis.core.utils.ManagerUtils.{BoundaryPathGroup, CountryPathSet, Path}
import it.agilelab.bigdata.gis.core.utils.{Configuration, ConfigurationProperties, Logger, ObjectPickler}
import it.agilelab.bigdata.gis.domain.configuration.IndexManagerConfiguration
import it.agilelab.bigdata.gis.domain.loader.{OSMAdministrativeBoundariesLoader, OSMGenericStreetLoader}
import it.agilelab.bigdata.gis.domain.models.{OSMBoundary, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import java.io.File
import scala.collection.immutable

case class IndexManager(conf: Config) extends Configuration with Logger {

  val indexConfig: IndexManagerConfiguration = IndexManagerConfiguration(conf)
  val pathManager : PathManager = PathManager(indexConfig.pathConf)
  val boundariesLoader : OSMAdministrativeBoundariesLoader = OSMAdministrativeBoundariesLoader(indexConfig.boundaryConf, pathManager)
  val indexSet: IndexSet = createIndexSet(indexConfig.inputPaths)

  case class IndexStuffs(regionIndex: Seq[OSMBoundary], cityIndex: Seq[OSMBoundary])

  case class IndexSet(boundaries: GeometryList[OSMBoundary], regions: GeometryList[OSMBoundary], streets: GeometryList[OSMStreetAndHouseNumber])

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

    val multiCountriesPathSet: Seq[CountryPathSet] =
      new File(upperFolderPath)
        .listFiles()
        .map(pathManager.getCountryPathSet)
        .toSeq

    logger.info("Loading OSM boundaries file into GeometryList...")


      val indexStuffs: List[IndexStuffs] =
        multiCountriesPathSet
          .par
          .map(countryPathSet => createCountryBoundaries(countryPathSet.boundary, boundariesLoader))
          .toList

      val cityIndexStuff: immutable.Seq[OSMBoundary] = indexStuffs.flatMap(_.cityIndex)
      val regionIndexStuff = indexStuffs.flatMap(_.regionIndex)

      val boundariesGeometryList: GeometryList[OSMBoundary] = boundariesLoader.buildIndex(cityIndexStuff.toList)

      val regionGeometryList: GeometryList[OSMBoundary] =
        if (regionIndexStuff.nonEmpty)
          boundariesLoader.buildIndex(regionIndexStuff)
        else
          null

      logger.info("Done loading OSM boundaries file into GeometryList!")

      val roads: Seq[Path] = multiCountriesPathSet.flatMap(countryPathSet => countryPathSet.roads)
      val addresses: Seq[Path] = multiCountriesPathSet.flatMap(countryPathSet => countryPathSet.addresses)

      val streetsGeometryList: GeometryList[OSMStreetAndHouseNumber] = createAddressesIndex(roads, addresses)

      //trigger garbage collector to remove the addressNumberGeometryList if still in memory
      System.gc()

      IndexSet(boundariesGeometryList, regionGeometryList, streetsGeometryList)

  }

  def createCountryBoundaries(paths: BoundaryPathGroup, boundariesLoader: OSMAdministrativeBoundariesLoader): IndexStuffs = {

    val boundaries: List[Seq[OSMBoundary]] =
      List(paths.city, paths.county, paths.region, paths.country)
        .map(pathList => pathList.flatMap(path => boundariesLoader.loadObjects(path)))

    val cities = boundaries(CITIES)
    val counties = boundaries(COUNTIES)
    val regions = boundaries(REGIONS)
    val countryBoundary = boundaries(COUNTRY).head

    val countryName = countryBoundary.country.getOrElse("UNDEFINED COUNTRY")

    logger.info(s"Start loading boundary of: $countryName...")

    val citiesWithCounties: Seq[OSMBoundary] =
      if (cities.isEmpty) counties
      else
        cities.map{ boundary =>

          val coveringCounty: Option[OSMBoundary] =
          counties
            .filter(county => county.customCovers(boundary))
            .find(county =>
              boundary.multiPolygon.getInteriorPoint.coveredBy(county.multiPolygon)
            )

          coveringCounty match {
            case None => boundary
            case Some(coveringBoundary) => boundary.copy(county = coveringBoundary.county)
          }
        }

    val countiesWithRegion: Seq[OSMBoundary] =
      if (citiesWithCounties.isEmpty) regions
      else
        citiesWithCounties.map{boundary =>

          val coveringRegion: Option[OSMBoundary] =
            regions
              .filter(region => region.customCovers(boundary))
            .find(region =>
              boundary.multiPolygon.getInteriorPoint.coveredBy(region.multiPolygon)
            )

          coveringRegion match {
            case None => boundary
            case Some(coveringBoundary) => boundary.copy(region = coveringBoundary.region)
          }
        }

    val primaryIndexBoundaries: Seq[OSMBoundary] = countiesWithRegion.map(_.copy(country = countryBoundary.country))
    val secondaryIndexBoundaries: Seq[OSMBoundary] = regions.map(_.copy(country = countryBoundary.country))

    logger.info(s"$countryName loaded.")

    IndexStuffs(secondaryIndexBoundaries, primaryIndexBoundaries)
  }

  /** Create the addresses index that will be used to decorate the road index leaves by adding
    * a sequence of OSMAddress to retrieve the candidate street number
    *
    */
  def createAddressesIndex(roads: Seq[Path], addresses: Seq[Path]): GeometryList[OSMStreetAndHouseNumber] = {

    val countryName = roads.head.split("/").reverse.tail.head

    logger.info(s"Loading OSM roads of $countryName...")

    val roadsGeometryList =
      OSMGenericStreetLoader(roads, addresses)
        .loadIndex(roads: _*)

    logger.info(s"Done loading OSM roads of $countryName!")

    roadsGeometryList
  }

  private def createIndexSet(inputPaths: List[String]): IndexSet = {
    if(inputPaths.length == 1) {
      val index = makeIndices(inputPaths.head)
      IndexSet(index.boundaries, index.regions, index.streets)
    }
    else {
      val boundaries = ObjectPickler.unpickle[GeometryList[OSMBoundary]](inputPaths.head)
      val regions = ObjectPickler.unpickle[GeometryList[OSMBoundary]](inputPaths(1))
      val streets = ObjectPickler.unpickle[GeometryList[OSMStreetAndHouseNumber]](inputPaths(2))
      IndexSet(boundaries, regions, streets)
    }
  }
}