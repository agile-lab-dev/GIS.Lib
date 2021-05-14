package it.agilelab.gis.domain.loader

import java.util

import com.vividsolutions.jts.geom._
import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.domain.models.OSMPlace
import it.agilelab.gis.domain.spatialList.GeometryList

object OSMPlaceShapeLoader {

  //Pay attention to side effects

  var index: GeometryList[OSMPlace] = _

  def getStreetIndex(path: String): GeometryList[OSMPlace] = {
    if (index == null) {
      index = new OSMPlaceShapeLoader().loadIndex(path)
    }
    index
  }

}

class OSMPlaceShapeLoader() extends Loader[OSMPlace] {
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    val poligons: Seq[(Geometry, util.List[AnyRef])] = if (source.endsWith("places_a_free_1.shp")) {
      ShapeFileReader.readMultiPolygonFeatures(source).map { case (mp, sp) => (mp, sp.getAttributes) }
    } else if (source.endsWith("places_free_1.shp")) {
      ShapeFileReader.readPointFeaturesToPolygon(source)
    } else {
      Seq.empty
    }

    poligons.map { case (geometry, list) => (list.toArray, geometry) }.toIterator

  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMPlace = {

    val name: String = Option(fields(5)).map(_.toString).getOrElse("")
    val placeType: String = Option(fields(3)).map(_.toString).getOrElse("")
    val firstField: String = Option(fields(1)).map(_.toString).getOrElse("")
    val secondField: Int = fields(2).asInstanceOf[Int]
    val fourthField: Long = fields(4).asInstanceOf[Long]

    OSMPlace(line, name, placeType, firstField, secondField, fourthField)
  }
}
