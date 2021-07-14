package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.{ Geometry, Point }

class GeometriesOperations {

  def getIntersectionPoints(geometry1: Geometry, geometry2: Geometry, points: List[Point]): List[Point] = {
    val intersection: Geometry = geometry1.intersection(geometry2)
    points.filter(point => intersection.contains(point))
  }
}
