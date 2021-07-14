package it.agilelab.gis.domain.managers

import com.vividsolutions.jts.geom.Geometry

/** Collector of all the operations that are performed on the geometries.
  */
object GeometriesOperations {

  /** Compute the minimum distance between a geometry and another one
    *
    * @param geometry1 first geometry
    * @param geometry2 second geometry
    * @return the distance between the two geometries
    */
  def computeDistance(geometry1: Geometry, geometry2: Geometry): Double = geometry1.distance(geometry2)

}
