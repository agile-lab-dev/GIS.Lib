package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.models.{OSMBoundary, OSMPlace, OSMStreet, PartialAddress}
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

      val boundariesShpFiles = subFolders.flatMap(subFolder =>
        subFolder.listFiles(new FilenameFilter {
          override def accept(dir: File, name: String): Boolean = name.endsWith("AL8.shp")
        })
      ).map(_.getAbsolutePath)

      boundariesGeometryList = boundariesLoader.loadIndex(boundariesShpFiles: _*)

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

    PartialAddress(road.map(_.street).getOrElse(""),place.map(_.name).getOrElse(""))

  }


}
