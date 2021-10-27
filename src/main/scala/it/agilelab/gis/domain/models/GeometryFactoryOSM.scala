package it.agilelab.gis.domain.models

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory
import org.locationtech.jts.geom._

object GeometryFactoryOSM {

  val factory =
    new GeometryFactory(
      new PrecisionModel(),
      8003,
      CoordinateArraySequenceFactory.instance()
    )

  def getLineString(points: Array[Coordinate]): LineString =
    factory.createLineString(points)

  def getPoint(x: Double, y: Double): Point =
    factory.createPoint(new Coordinate(x, y))

}
