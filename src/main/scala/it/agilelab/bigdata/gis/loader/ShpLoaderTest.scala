package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.models.{OSMPlace, OSMStreet, PartialAddress}
import it.agilelab.bigdata.gis.spatialList.GeometryList
import it.agilelab.bigdata.gis.spatialOperator.KNNQueryMem

object ShpLoaderTest extends App {

  val path = "/home/stefano/Documents/IntesaSmartCareCore/maps"

  println("[GISLib] Loading OSM places file into GeometryList...")

  val placesLoader  = new OSMPlaceShapeLoader()
  val roadsLoader = new OSMStreetShapeLoader
  //val loader  = new OSMStreetShapeLoader()
  val folder: File = new File(path)
  val subFolders: Array[File] = folder.listFiles(
    new FilenameFilter {
    override def accept(dir: File, name: String): Boolean = name.endsWith(".shp")
    }
  )

  println("[GISLib] Loading OSM places file into GeometryList...")

  val placesShpFiles: Array[String] = subFolders.flatMap( subFolder =>
    subFolder.listFiles( new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith("places_free_1.shp") || name.endsWith("places_a_free_1.shp")
    })
  ).map(_.getAbsolutePath)

  val placesGeometryList: GeometryList[OSMPlace] = placesLoader.loadIndex(placesShpFiles: _*)

  println("[GISLib] Done loading OSM places file into GeometryList!")

  println("[GISLib] Loading OSM roads file into GeometryList...")

  val roadsShpFiles: Array[String] = subFolders.flatMap( subFolder =>
    subFolder.listFiles( new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith("roads_free_1.shp")
    })
  ).map(_.getAbsolutePath)

  val roadsGeometryList: GeometryList[OSMStreet] = roadsLoader.loadIndex(roadsShpFiles: _*)

  println("[GISLib] Done loading OSM roads file into GeometryList!")


  val latitude = 39.832223

  val longitude = 18.341623

  /*val latitude = 45.821003

  val longitude = 6.950784*/

  /*val latitude = 45.473273
  val longitude = 9.164086*/


  val fact = new GeometryFactory()
  val queryPoint: Point = fact.createPoint(new Coordinate(longitude, latitude))

  val placesResult: Seq[OSMPlace] = KNNQueryMem.SpatialKnnQuery(
    placesGeometryList,
    queryPoint,
    10)
  val placesList: Seq[OSMPlace] =  placesResult.seq

  val roadsResult: Seq[OSMStreet] = KNNQueryMem.SpatialKnnQuery(
    roadsGeometryList,
    queryPoint,
    1)
  val roadsList: Option[OSMStreet] =  roadsResult.seq.headOption

  val candidates: Seq[OSMPlace] = roadsList.map(x => {
    placesList.filter( y => y.polygon.contains(x))
  }).getOrElse(Seq.empty[OSMPlace])

  val res: PartialAddress = if (candidates.isEmpty) PartialAddress(roadsList.map(_.street).getOrElse(""), placesList.headOption.map(_.name).getOrElse(""))
  else PartialAddress(roadsList.map(_.street).getOrElse(""), candidates.headOption.map(_.name).getOrElse(""))


  println("Result of reverse-geocoding operation: " + res.toString)


}
