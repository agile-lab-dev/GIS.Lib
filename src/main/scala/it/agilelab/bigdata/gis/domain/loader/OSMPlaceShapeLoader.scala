package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom._
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models._
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList


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

    if (source.endsWith("places_a_free_1.shp")) ShapeFileReader.readMultiPolygonFeatures(source).map(e => (e._2.toArray, e._1)).toIterator
    else if (source.endsWith("places_free_1.shp")) ShapeFileReader.readPointFeaturesToPolygon(source).map(e => (e._2.toArray, e._1)).toIterator
    else Iterator.empty

  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMPlace = {

    val name: String = if (fields(5) != null) fields(5).toString else ""
    val placetype: String = if (fields(3) != null) fields(3).toString else ""
    val firstField: String = if (fields(1) != null) fields(1).toString else ""
    val secondField: Int = fields(2).asInstanceOf[Int]
    val fourthField: Long = fields(4).asInstanceOf[Long]

    OSMPlace(line, name, placetype, firstField, secondField, fourthField)

  }


}
