package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.models.{OSMPlace, OSMStreet, PartialAddress}
import it.agilelab.bigdata.gis.spatialList.GeometryList
import it.agilelab.bigdata.gis.spatialOperator.KNNQueryMem

object RGManager {

  val placesLoader  = new OSMPlaceShapeLoader
  val roadsLoader = new OSMStreetShapeLoader

  var subFolders: Array[File] = _
  var placesGeometryList: GeometryList[OSMPlace] = _
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
    if (placesGeometryList == null) {

      println("[GISLib] Loading OSM places file into GeometryList...")

      val placesShpFiles = subFolders.flatMap(subFolder =>
        subFolder.listFiles(new FilenameFilter {
          override def accept(dir: File, name: String): Boolean = /*name.endsWith("places_free_1.shp") ||*/ name.endsWith("places_a_free_1.shp")
        })
      ).map(_.getAbsolutePath)

      placesGeometryList = placesLoader.loadIndex(placesShpFiles: _*)

      println("[GISLib] Done loading OSM places file into GeometryList!")

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

    val placesResult: Seq[OSMPlace] = KNNQueryMem.SpatialKnnQuery(
      placesGeometryList,
      queryPoint,
      1)
    val place: Option[OSMPlace] =  placesResult.seq.headOption

    PartialAddress(road.map(_.street).getOrElse(""),place.map(_.name).getOrElse(""), "", "", "")

  }

}
