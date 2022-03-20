package it.agilelab.gis.domain.loader

import it.agilelab.gis.core.loader.Loader
import java.util

import org.locationtech.jts.geom.{ Geometry, MultiPolygon, OSMSea }

class OSMSeaLoader() extends Loader[OSMSea] {
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    val poligons: Seq[(MultiPolygon, util.List[AnyRef])] = ShapeFileReader
      .readMultiPolygonFeatures(source)
      .map { case (mp, sp) => (mp, sp.getAttributes) }

    poligons.map { case (geometry, list) => (list.toArray, geometry) }.toIterator

  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMSea = {
    val x: Int = fields(1).asInstanceOf[Int]
    val y: Int = fields(2).asInstanceOf[Int]

    OSMSea(line, x, y)
  }
}
