package it.agilelab.gis.domain.managers

import it.agilelab.gis.core.utils.DistanceUtils
import org.locationtech.jts.geom.{ Geometry, GeometryFactory, MultiPoint, Point }

/** Collector of all the operations that are performed on the geometries.
  */
object GeometriesOperations {

  /** Compute the distance in meters between two points taking into account the shape of the earth,
    * using the Haversine formula
    *
    * @param point1 first point
    * @param point2 second point
    * @return the distance between the two points
    */
  def computePointsDistance(point1: Point, point2: Point): Double =
    DistanceUtils.haversineFormula(point1.getY, point1.getX, point2.getY, point2.getX)

  /** Get the list of points which are inside the intersection of two geometries
    *
    * @param geometry1 first geometry
    * @param geometry2 second geometry
    * @param points array of points
    * @return the sublist of points within the intersection as a MultiPoint
    */
  def getIntersectionPoints(geometry1: Geometry, geometry2: Geometry, points: Array[Point]): MultiPoint = {
    val intersection: Geometry = geometry1.intersection(geometry2)
    val pointsIntersected: Array[Point] = points.filter(point => intersection.contains(point))
    new GeometryFactory().createMultiPoint(pointsIntersected)
  }

  /** Verify that a geometry is contained into another geometry
    *
    * @param geometry1 reference geometry
    * @param geometry2 geometry to be compared
    * @return true: if geometry1 contains geometry2 | false: otherwise
    */
  def contains(geometry1: Geometry, geometry2: Geometry): Boolean = geometry1.contains(geometry2)

}
