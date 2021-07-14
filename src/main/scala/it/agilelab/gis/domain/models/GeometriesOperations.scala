package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

class GeometriesOperations {

  // Returns the minimum distance between this Geometry and another Geometry.
  def computeDistance(geometry1: Geometry, geometry2: Geometry): Double = geometry1.distance(geometry2)

}