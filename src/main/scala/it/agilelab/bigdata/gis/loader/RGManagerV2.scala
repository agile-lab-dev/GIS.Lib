package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.models.{OSMBoundary, OSMStreet, PartialAddress}
import it.agilelab.bigdata.gis.spatialList.GeometryList
import it.agilelab.bigdata.gis.spatialOperator.KNNQueryMem

object RGManagerV2 {

  val boundariesLoader  = new OSMAdministrativeBoundariesLoader
  val roadsLoader = new OSMStreetShapeLoader

  var subFolders: Array[File] = _
  var boundariesGeometryList: GeometryList[OSMBoundary] = _
  var roadsGeometryList: GeometryList[OSMStreet] = _

  def init(filesLocation: String) = {
    if(subFolders == null){
      //val loader  = new OSMStreetShapeLoader()
      val folder: File = new File(filesLocation)
      subFolders = folder.listFiles(
        new FilenameFilter {
          override def accept(dir: File, name: String): Boolean = name.endsWith(".shp")
        }
      )
    }
    if (boundariesGeometryList == null) {

      println("[GISLib] Loading OSM boundaries file into GeometryList...")

      val path = subFolders.filter(f => f.getAbsolutePath.endsWith("italy.shp")).map(_.getAbsolutePath).head

      val regionsFile: String = path.toString + "/World_AL4.shp"
      val countiesFile: String = path + "/World_AL6.shp"
      val citiesFile: String = path + "/World_AL8.shp"
      val regions: Seq[OSMBoundary] = boundariesLoader.loadObjects(regionsFile).toSeq
      val counties = boundariesLoader.loadObjects(countiesFile).toSeq
      val cities = boundariesLoader.loadObjects(citiesFile).toSeq

      val countiesWithRegions = counties.map{ x => {
          //For the sake of simplicity we rely on the fact that the interiorPoint is always contained in the polygon and
          // that a point of a county is inside only one region
          val candidate_regions = regions.filter(y => x.polygon.getInteriorPoint.coveredBy(y.polygon))
          x.copy(region = candidate_regions.headOption.map(_.region.get))
        }
      }

      val citiesWithCountiesRegions = cities.map{ x => {
        //For the sake of simplicity we rely on the fact that the interiorPoint is always contained in the polygon and
        // that a point of a city is inside only one county
          val candidate_counties = countiesWithRegions.filter(y => x.polygon.getInteriorPoint.coveredBy(y.polygon))
          x.copy(county = candidate_counties.headOption.map(_.county.get), region = candidate_counties.headOption.map(_.region.get))
        }
      }

      boundariesGeometryList = boundariesLoader.buildIndex(citiesWithCountiesRegions.toIterator)

      println("[GISLib] Done loading OSM boundaries file into GeometryList!")

    }
    if (roadsGeometryList == null) {
      println("[GISLib] Loading OSM roads file into GeometryList...")

        val roadsShpFiles = subFolders.flatMap(subFolder =>
          subFolder.listFiles(new FilenameFilter {
            override def accept(dir: File, name: String): Boolean = name.endsWith("roads_free_1.shp")
          })
        ).map(_.getAbsolutePath)

      roadsGeometryList = roadsLoader.loadIndex(roadsShpFiles: _*)

      println("[GISLib] Done loading OSM roads file into GeometryList!")

    }

  }

  def reverseGeocode(latitude: Double, longitude: Double) : PartialAddress = {
    val fact = new GeometryFactory()
    val queryPoint: Point = fact.createPoint(new Coordinate(longitude, latitude))

    val roadsResult: Seq[OSMStreet] = KNNQueryMem.SpatialKnnQuery(
      roadsGeometryList,
      queryPoint,
      1)
    val road: Option[OSMStreet] =  roadsResult.seq.headOption

    val placesResult: Seq[OSMBoundary] = KNNQueryMem.SpatialKnnQuery(
      boundariesGeometryList,
      queryPoint,
      10)
    val place: Option[OSMBoundary] =  placesResult.map(x => (x,x.polygon.distance(queryPoint))).reduceOption((A, B)=> if(A._2 < B._2) A else B).map(_._1)

    PartialAddress(road.map(_.street).getOrElse(""), place.map(_.city.getOrElse("")).getOrElse(""), place.map(_.county.getOrElse("")).getOrElse(""), place.map(_.region.getOrElse("")).getOrElse(""), place.map(_.country.getOrElse("")).getOrElse(""))

  }


}
