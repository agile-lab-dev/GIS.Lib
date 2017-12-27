package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator
import com.vividsolutions.jts.geom
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.models.{OSMPlace, OSMStreet}
import it.agilelab.bigdata.gis.spatialList.GeometryList
import it.agilelab.bigdata.gis.spatialOperator.KNNQueryMem

import scala.collection.immutable

object ShpLoaderTest extends App {

  val path = "/home/stefano/Documents/IntesaSmartCareCore/maps"

  println("[GISLib] Loading OSM file into GeometryList...")

  val loader  = new OSMPlaceShapeLoader()
  //val loader  = new OSMStreetShapeLoader()
  val folder: File = new File(path)
  val subFolders: Array[File] = folder.listFiles(
    new FilenameFilter {
    override def accept(dir: File, name: String): Boolean = name.endsWith(".shp")
    }
  )
  val shpFiles: Array[String] = subFolders.flatMap( subFolder =>
    subFolder.listFiles( new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith("places_free_1.shp") || name.endsWith("places_a_free_1.shp")
    })
  ).map(_.getAbsolutePath)

  val geometryList: GeometryList[OSMPlace] = loader.loadIndex(shpFiles: _*)

  println("[GISLib] Done loading OSM file into GeometryList!")

  /*val latitude = 39.832223

  val longitude = 18.341623*/

  /*val latitude = 45.821003

  val longitude = 6.950784*/

  val latitude = 45.473273
  val longitude = 9.164086



  val fact = new GeometryFactory()
  val queryPoint: Point = fact.createPoint(new Coordinate(longitude, latitude))

  val queryResult: Seq[OSMPlace] = KNNQueryMem.SpatialKnnQuery(
    geometryList,
    queryPoint,
    1)
  val list: Seq[OSMPlace] =  queryResult.seq
  println("Results:")
  list.foreach{
    x =>
      println(x.toString())

  }

}
