package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

class GeometriesWrapper[T <: Geometry](geometry1: T, geometry2: T) {

  // Returns the minimum distance between this Geometry and another Geometry.
  def computeDistance: Double = geometry1.distance(geometry2)

}