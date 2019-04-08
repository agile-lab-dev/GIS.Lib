package it.agilelab.bigdata.gis.domain.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.core.managers.RGManager2
import it.agilelab.bigdata.gis.domain.models.{Address, OSMBoundary, OSMStreet}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem

class OSMManager extends RGManager2[OSMBoundary, OSMStreet, Address] {

  val boundariesLoader: OSMAdministrativeBoundariesLoader = new OSMAdministrativeBoundariesLoader
  val roadsLoader = new OSMStreetShapeLoader

  override var geometryList: GeometryList[OSMBoundary] = _
  override var geometryList2: GeometryList[OSMStreet] = _

  /**
    * Initializes all geo lists
    * @param argv [file location, prefix]
    * @throws IllegalArgumentException if input params are wrong
    */
  @throws[IllegalArgumentException]
  override def init(argv: String*): Unit = {

    val loc = argv.apply(0)
    val prefix = argv.apply(1)

    val folder: File = new File(loc)
    val subFolders: Array[File] = folder.listFiles(
      new FilenameFilter {
        override def accept(dir: File, name: String): Boolean = name.endsWith(".shp")
      }
    )

    if (checkGeoList) {
      geometryList = loadOSMBoundaries(subFolders, prefix)
    }
    if (checkGeoList2) {
      geometryList2 = loadOSMRoads(subFolders)
    }
  }

  override def reverseGeocode(latitude: Double, longitude: Double, filterEmptyStreets: Boolean = false, maxDistance: Double = 200, distanceOp: String = ""): Option[Address] = {
    val fact = new GeometryFactory()
    val queryPoint: Point = fact.createPoint(new Coordinate(longitude, latitude))

    val roadsResult: Seq[OSMStreet] = KNNQueryMem.SpatialKnnQuery(
      geometryList2,
      queryPoint,
      10)

    //If filterEmptyStreets is enabled then we filter out all the not defined and empty streets
    val road: Option[OSMStreet] =  roadsResult
      .find(p => !filterEmptyStreets || (p.street.isDefined && !p.street.get.trim.equals("")))

    val placesResult: Seq[OSMBoundary] = KNNQueryMem.SpatialKnnQuery(
      geometryList,
      queryPoint,
      10)
    val place: Option[OSMBoundary] =  placesResult.headOption

    Some(Address(road, place))

  }

  /*
    ---------------
    Support methods
    ---------------
   */
  private def loadOSMBoundaries(subFolders: Array[File], filePrefix: String = ""): GeometryList[OSMBoundary] = {
    println("[GISLib] Loading OSM boundaries file into GeometryList...")

    val path = subFolders.filter(f => f.getAbsolutePath.endsWith("italy.shp")).map(_.getAbsolutePath).head

    val regionsFile: String = path.toString + filePrefix + "4.shp"
    val countiesFile: String = path + filePrefix + "6.shp"
    val citiesFile: String = path + filePrefix + "8.shp"
    val regions: Seq[OSMBoundary] = boundariesLoader.loadObjects(regionsFile)
    val counties: Seq[OSMBoundary] = boundariesLoader.loadObjects(countiesFile)
    val cities: Seq[OSMBoundary] = boundariesLoader.loadObjects(citiesFile)

    val countiesWithRegions = counties.map {
      x => {
        // For the sake of simplicity we rely on the fact that the interiorPoint is always contained in the polygon and
        // that a point of a county is inside only one region
        val candidate_regions = regions.filter(y => x.multiPolygon.getInteriorPoint.coveredBy(y.multiPolygon))
        x.copy(region = candidate_regions.headOption.map(_.region.get))
      }
    }

    val citiesWithCountiesRegions: Seq[OSMBoundary] = cities.map {
      x => {
        // For the sake of simplicity we rely on the fact that the interiorPoint is always contained in the polygon and
        // that a point of a city is inside only one county
        val candidate_counties = countiesWithRegions.filter(y => x.multiPolygon.getInteriorPoint.coveredBy(y.multiPolygon))
        x.copy(county = candidate_counties.headOption.map(_.county.get), region = candidate_counties.headOption.map(_.region.get))
      }
    }

    val res: GeometryList[OSMBoundary] = boundariesLoader.buildIndex(citiesWithCountiesRegions.toList)

    println("[GISLib] Done loading OSM boundaries file into GeometryList!")
    res
  }

  private def loadOSMRoads(subFolders: Array[File]): GeometryList[OSMStreet] = {

    println("[GISLib] Loading OSM roads file into GeometryList...")

    val roadsShpFiles = subFolders.flatMap(subFolder =>
      subFolder.listFiles(new FilenameFilter {
        override def accept(dir: File, name: String): Boolean = name.endsWith("roads_free_1.shp")
      })
    ).map(_.getAbsolutePath)

    val res: GeometryList[OSMStreet] = roadsLoader.loadIndex(roadsShpFiles: _*)
    println("[GISLib] Done loading OSM roads file into GeometryList!")
    res
  }

}