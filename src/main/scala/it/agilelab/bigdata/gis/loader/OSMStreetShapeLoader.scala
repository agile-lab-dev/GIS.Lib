package it.agilelab.bigdata.gis.loader

import org.geotools.data.simple._
import org.opengis.feature.simple._
import org.geotools.data.shapefile._
import com.vividsolutions.jts.{geom => jts}
import java.net.URL
import java.io.File
import java.util

import com.vividsolutions.jts.geom.{Geometry, LineString, MultiLineString}
import it.agilelab.bigdata.gis.enums.IndexType
import it.agilelab.bigdata.gis.models._
import it.agilelab.bigdata.gis.spatialList.GeometryList
import org.opengis.feature.Attribute

import scala.collection.mutable
import scala.collection.JavaConversions._


object OSMStreetShapeLoader{

  //Pay attention to side effects

  var index: GeometryList[OSMStreet] = null
  def getStreetIndex(path: String) = {
    if (index == null){
      index = new OSMStreetShapeLoader().loadIndex(path)
    }
    index
  }

}

class OSMStreetShapeLoader() extends Loader[OSMStreet]{
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    val lines: Iterator[(Array[AnyRef], MultiLineString)] = ShapeFileReader.readMultiLineFeatures(source).map(e => (e._2.toArray, e._1)).toIterator
    lines
  }

  protected def objectMapping(fields: Array[AnyRef],line: Geometry): OSMStreet = {
    val streetType: String = if(fields(3) != null) fields(3).toString else ""
    val st = streetType match {
      case "motorway" => OSMStreetType.Motorway
      case "secondary" => OSMStreetType.Secondary
      case "unclassified" => OSMStreetType.Unclassified
      case "tertiary" => OSMStreetType.Tertiary
      case "primary_link" => OSMStreetType.Primary_link
      case "primary" => OSMStreetType.Primary
      case "track" => OSMStreetType.Track
      case "residential" => OSMStreetType.Residential
      case "pedestrian" => OSMStreetType.Pedestrian
      case "trunk_link" => OSMStreetType.Trunk_Link
      case "motorway_link" => OSMStreetType.Motorway_Link
      case "footway" => OSMStreetType.Footway
      case "service" => OSMStreetType.Service
      case "path" => OSMStreetType.Path
      case "cycleway" => OSMStreetType.Cycleway
      case "track_grade2" => OSMStreetType.Track_Grade2
      case "steps" => OSMStreetType.Steps
      case _ => OSMStreetType.Unclassified
    }

    //val length: Double = fields(8).replace(',', '.').toDouble
    //var bidirected: Boolean = false
    //if (fields(5) == 1) bidirected = true
    val street: String = if(fields(4) != null) fields(4).toString else ""
    val code: String = if(fields(5) != null) fields(5).toString else ""
    val oneway: Boolean = if(fields(6).toString == "F") false else true
    val isTunnel: Boolean = if(fields(10).toString == "F") false else true
    val isBridge: Boolean = if(fields(9).toString == "F") false else true
    val speedlimit: Int = if(fields(7).toString.toInt != 0) fields(7).toString.toInt else 0
    //val toSpeed: Integer = fields(6).toInt


    OSMStreet(line, street, code, isBridge, isTunnel, speedlimit, oneway, st)
  }

}
