package it.agilelab.bigdata.gis.loader

import java.io.{File, FilenameFilter}

import it.agilelab.bigdata.gis.models.{OSMPlace, OSMStreet}

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



}
