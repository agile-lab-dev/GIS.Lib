package it.agilelab.gis.core.model.geometry

import com.vividsolutions.jts.geom._
import it.agilelab.gis.core.utils.DistanceUtils

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
    val factory = new GeometryFactoryEnriched()
    new Circle(point, radius, factory)
  }
}

/** Class extends Geometry and represent a Circle
  *
  * @param center a Point representing the center of the circle
  * @param radius Double representing the radius of the circle
  * @param circleFactory a GeometryFactory needed to create the geometry
  */
case class Circle(center: Point, radius: Double, circleFactory: GeometryFactoryEnriched)
    extends Geometry(circleFactory: GeometryFactoryEnriched) {

  private lazy val coordinates = circleFactory.getCoordinateSequenceFactory.create(getCoordinates());
  protected lazy val shell: LinearRing = factory.createLinearRing(coordinates)

  /** Define a shell: a square (built as LinearRing) that contains the circle
    */
  override def getCoordinates(): Array[Coordinate] = {
    val coordinates_0 = new Coordinate(getMBR.getMinX, getMBR.getMinY)
    val coordinates_1 = new Coordinate(getMBR.getMaxX, getMBR.getMinY)
    val coordinates_2 = new Coordinate(getMBR.getMaxX, getMBR.getMaxY)
    val coordinates_3 = new Coordinate(getMBR.getMinX, getMBR.getMaxY)
    Array[Coordinate](coordinates_0, coordinates_1, coordinates_2, coordinates_3, coordinates_0)
  }

  def apply(geomComponentFilter: GeometryComponentFilter): Unit = geomComponentFilter.filter(this)
  def apply(geomFilter: GeometryFilter): Unit = geomFilter.filter(this)
  def apply(coordSeqFilter: CoordinateSequenceFilter): Unit =
    if (!this.isEmpty) {
      coordSeqFilter.filter(this.coordinates, 0)
      if (coordSeqFilter.isGeometryChanged) this.geometryChanged()
    }
  def apply(coordFilter: CoordinateFilter): Unit =
    if (!this.isEmpty) {
      coordFilter.filter(this.getCoordinate)
    }

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
    * @param other the geometry to check against
    * @return true, if other geometry is completely or partially contained
    */
  override def contains(other: Geometry): Boolean =
    if (
      DistanceUtils.haversineFormula(
        this.center.getY,
        this.center.getX,
        other.getInteriorPoint.getY,
        other.getInteriorPoint.getX) < radius
    ) // TODO: currently it behaves like intersect
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

  /** Compute the distance from the circle side
    *
    * @param geom other geometry to compare with
    * @return the distance from the circle side (zero if the geometry is within the circle)
    */
  override def distance(geom: Geometry) =
    if (!this.contains(geom))
      DistanceUtils.haversineFormula(
        this.center.getY,
        this.center.getX,
        geom.getInteriorPoint.getY,
        geom.getInteriorPoint.getX) - radius
    else 0.0

  /** Geometry type
    *
    * @return always string "Circle"
    */
  override def getGeometryType: String = "Circle"

  /** Coordinates
    *
    * @return coordinates of the circle center point
    */
  override def getCoordinate: Coordinate =
    if (isEmpty()) null
    else new Coordinate(center.getX, center.getY, 0)

  /** NumPoints
    *
    * @return the number of coordinates the circle is made of (always 1, the center)
    */
  override def getNumPoints: Int = 1

  /** is Empty
    *
    * @return boolean
    */
  override def isEmpty: Boolean = shell.isEmpty

  /** Dimension of the circle geometry
    *
    * @return always 2 (it's a 2D circle)
    */
  override def getDimension: Int = 2

  /** Boundaries
    *
    * @return the shell containing the circle
    */
  override def getBoundary: Geometry = shell

  /** Boundary dimension
    *
    * @return dimension of the boundary containing the circle (always 2D)
    */
  override def getBoundaryDimension: Int = 2

  /** Reverse of a circle is the circle itself
    *
    * @return the circle
    */
  override def reverse(): Geometry = this

  /** Check if a second geometry is an exact match with this circle
    *
    * @param other the geometry to be compared
    * @param tolerance
    * @return boolean, true if the 2 geometries are the same
    */
  override def equalsExact(other: Geometry, tolerance: Double): Boolean =
    if (!isEquivalentClass(other)) false
    else {
      if (shell == other.getBoundary) true
      else false
    }

  /** Normalization of the circle
    */
  override def normalize(): Unit = {}

  /** Internal Envelop
    *
    * @return the internal envelop of the shell containing the circle
    */
  override def computeEnvelopeInternal(): Envelope = shell.getEnvelopeInternal

  /** Returns whether this Geometry is
    * greater than, equal to, or less than
    * another Geometry of the same class
    * @param o
    * @return 1 if this is grater then
    *         0 if this is the same size
    *         -1 if this is smaller then
    */
  override def compareToSameClass(o: Any): Int = {
    val other = o.asInstanceOf[Circle]
    if (this.radius > other.radius) 1
    else if (this.radius < other.radius) -1
    else 0
  }

  /** Returns whether this Geometry is
    * greater than, equal to, or less than
    * another Geometry of the same class
    * @param o
    * @return 1 if this is grater then
    *         0 if this is the same size
    *         -1 if this is smaller then
    */
  override def compareToSameClass(o: Any, comp: CoordinateSequenceComparator): Int = {
    val other = o.asInstanceOf[Circle]
    if (this.radius > other.radius) 1
    else if (this.radius < other.radius) -1
    else 0
  }
}
