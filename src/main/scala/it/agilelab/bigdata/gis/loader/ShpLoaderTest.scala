package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.models.{OSMPlace, OSMStreet}
import it.agilelab.bigdata.gis.spatialOperator.KNNQueryMem

object ShpLoaderTest extends App {

  val path = "/home/stefano/Documents/IntesaSmartCareCore/maps"

  println("[GISServer] Loading OSM file into GeometryList...")

  val loader  = new OSMPlaceShapeLoader()
  //val loader  = new OSMStreetShapeLoader()
  val folder: File = new File(path)
  val subFolders: Array[File] = folder.listFiles(
    new FilenameFilter {
    override def accept(dir: File, name: String): Boolean = name.endsWith(".shp")
    }
  )
  val shpFiles: Array[String] = subFolders.flatMap(subFolder =>
    subFolder.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith("places_a_free_1.shp")
    })
  ).map(_.getAbsolutePath)
  val geometryList = loader.loadIndex(shpFiles: _*)
  println("[GISServer] Done loading OSM file into GeometryList!")

  val latitude = 40.21344

  val longitude = 9.342532

  val fact = new GeometryFactory()
  val queryPoint: Point = fact.createPoint(new Coordinate(longitude, latitude))

  val queryResult: Seq[OSMPlace] = KNNQueryMem.SpatialKnnQuery(
    geometryList,
    queryPoint,
    1)
  println("Result: " + queryResult.toString)
  println("First Result: " + queryResult.headOption)





}
