package it.agilelab.bigdata.gis.loader

import org.geotools.data.simple._
import org.opengis.feature.simple._
import org.geotools.data.shapefile._
import com.vividsolutions.jts.{geom => jts}
import java.net.URL
import java.io.File
import java.util

import com.vividsolutions.jts.geom.{Geometry, LineString, MultiLineString, MultiPolygon}
import it.agilelab.bigdata.gis.enums.IndexType
import it.agilelab.bigdata.gis.models._
import it.agilelab.bigdata.gis.spatialList.GeometryList
import org.opengis.feature.Attribute

import scala.collection.mutable
import scala.collection.JavaConversions._


object OSMPlaceShapeLoader{

  //Pay attention to side effects

  var index: GeometryList[OSMPlace] = null
  def getStreetIndex(path: String) = {
    if (index == null){
      index = new OSMPlaceShapeLoader().loadIndex(path)
    }
    index
  }

}

class OSMPlaceShapeLoader() extends Loader[OSMPlace]{
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    val lines: Iterator[(Array[AnyRef], MultiPolygon)] = ShapeFileReader.readMultiPolygonFeatures(source).map(e => (e._2.toArray, e._1)).toIterator
    lines
  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMPlace = {

    val name: String = if(fields(5) != null) fields(3).toString else ""
    val placetype: String = if(fields(3) != null) fields(5).toString else ""
    val firstField: String = if(fields(1) != null) fields(1).toString  else ""
    val secondField: Int = fields(2).asInstanceOf[Int]
    val fourthField: Long = fields(4).asInstanceOf[Long]


    OSMPlace(line, name, placetype, firstField, secondField, fourthField)
  }


}
