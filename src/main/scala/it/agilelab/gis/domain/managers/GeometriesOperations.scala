package it.agilelab.gis.domain.managers

import com.vividsolutions.jts.geom.{ Geometry, Point }

/** Collector of all the operations that are performed on the geometries.
  */
object GeometriesOperations {

  /** Get the list of points which are inside the intersection of two geometries
    *
    * @param geometry1 first geometry
    * @param geometry2 second geometry
    * @param points list of points
    * @return the sublist of points within the intersection
    */
  def getIntersectionPoints(geometry1: Geometry, geometry2: Geometry, points: List[Point]): List[Point] = {
    val intersection: Geometry = geometry1.intersection(geometry2)
    points.filter(point => intersection.contains(point))
  }
}
