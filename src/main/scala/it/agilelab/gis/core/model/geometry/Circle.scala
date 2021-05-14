package it.agilelab.gis.core.model.geometry

import java.io.Serializable

import com.vividsolutions.jts.geom.{ Coordinate, Envelope, GeometryFactory, Point }

/** @author andreaL
  */
object Circle {

  /** MB rto circle.
    *
    * @param mbr the mbr
    * @return the circle
    */
  def MBRtoCircle(mbr: Envelope): Circle = {
    val radius = (mbr.getMaxX - mbr.getMinX) / 2
    val fact = new GeometryFactory
    val coordinate = new Coordinate(mbr.getMinX + radius, mbr.getMinY + radius)
    val point = fact.createPoint(coordinate)
    new Circle(point, radius)
  }
}

case class Circle(center: Point, radius: Double) extends Serializable {

  /** Gets the mbr.
    *
    * @return the mbr
    */
  def getMBR: Envelope =
    new Envelope(
      center.getX - radius,
      center.getX + radius,
      center.getY - radius,
      center.getY + radius
    )

  /** Contains.
    *
    * @param point the point
    * @return true, if successful
    */
  def contains(point: Point): Boolean =
    if (center.distance(point) < radius)
      true
    else
      false

  /** Intersects.
    *
    * @param point the point
    * @return true, if successful
    */
  def intersects(point: Point): Boolean =
    if (this.center.distance(point) <= this.radius)
      true
    else
      false

  /** Intersects.
    *
    * @param e the e
    * @return true, if successful
    */
  def intersects(e: Envelope): Boolean = {
    val cx: Double = this.center.getX
    val cy: Double = this.center.getY
    val radius: Double = this.radius
    val recx: Double = (e.getMinX + e.getMaxX) / 2
    val rectwidth: Double = e.getMaxX - e.getMinX
    val rectheight: Double = e.getMaxY - e.getMinY
    val recy: Double = (e.getMaxY + e.getMinY) / 2
    val circleDistancex: Double = Math.abs(cx - recx)
    val circleDistancey: Double = Math.abs(cy - recy)

    if (circleDistancex > (rectwidth / 2 + radius))
      false
    else if (circleDistancey > (rectheight / 2 + radius))
      false
    else if (circleDistancex <= (rectwidth / 2))
      true
    else if (circleDistancey <= (rectheight / 2))
      true
    else {
      val cornerDistance_sq: Double =
        (circleDistancex - rectwidth / 2) * (circleDistancex - rectwidth / 2) +
          (circleDistancey - rectheight / 2) * (circleDistancey - rectheight / 2)

      cornerDistance_sq <= (radius * radius)
    }
  }

}
