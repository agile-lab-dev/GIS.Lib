package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

/** Collector of all the operations that are performed on the geometries.
  */
object GeometriesOperations {

  /** Verify that a geometry is contained into another geometry
    *
    * @param geometry1 reference geometry
    * @param geometry2 geometry to be compared
    * @return true: if geometry1 contains geometry2 | false: otherwise
    */
  def contains(geometry1: Geometry, geometry2: Geometry): Boolean = geometry1.contains(geometry2)

}
