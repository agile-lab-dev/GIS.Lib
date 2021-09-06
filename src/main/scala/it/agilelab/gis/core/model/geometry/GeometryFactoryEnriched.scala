package it.agilelab.gis.core.model.geometry

import com.vividsolutions.jts.geom._

/**  GeometryFactory class extended in order to add Circle related functions
  */
class GeometryFactoryEnriched extends GeometryFactory {

  /** Create a circle
    *
    * @param center Point representing the desired center of the circle
    * @param radius Doube representing the desired radius of the circle
    * @return a Circle
    */
  def createCircle(center: Point, radius: Double): Circle =
    Circle(center, radius, this)
}
