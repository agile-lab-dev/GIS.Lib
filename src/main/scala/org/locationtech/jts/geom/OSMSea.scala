package org.locationtech.jts.geom

import org.locationtech.jts.geom.impl.CoordinateArraySequence

case class OSMSea(
    polygon: Geometry,
    x: Int,
    y: Int
) extends Geometry(polygon.getFactory) {

  override def toString: String =
    s"""Line: ${polygon.toString}
       |x: $x,
       |y: $y
       """.stripMargin

  /** As seen from class Street, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
  def apply(filter: CoordinateFilter): Unit = polygon.apply(filter)

  def apply(filter: CoordinateSequenceFilter): Unit = polygon.apply(filter)

  def apply(filter: GeometryFilter): Unit = polygon.apply(filter)

  def apply(filter: GeometryComponentFilter): Unit = polygon.apply(filter)

  def getCoordinateSequence: CoordinateArraySequence =
    new CoordinateArraySequence(getCoordinates)

  override def computeEnvelopeInternal(): Envelope =
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

  override def getBoundary: Geometry = polygon.getBoundary

  override def compareToSameClass(o: scala.Any): Int = {
    val w: OSMSea = o.asInstanceOf[OSMSea]
    // MD - optimized implementation
    var i: Int = 0
    var j: Int = 0
    while (i < getNumPoints && j < w.getNumPoints) {
      val comparison: Int = getCoordinateSequence.getCoordinate(i).compareTo(w.getCoordinateSequence.getCoordinate(j))
      if (comparison != 0) return comparison
      i += 1
      j += 1
    }

    if (i < getNumPoints) 1
    else if (j < w.getNumPoints) -1
    else 0
  }

  override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
    val w: OSMSea = o.asInstanceOf[OSMSea]
    comp.compare(getCoordinateSequence, w.getCoordinateSequence)
  }

  override def getCoordinates: Array[Coordinate] = polygon.getCoordinates

  override def getDimension: Int = polygon.getDimension

  override def getGeometryType: String = polygon.getGeometryType

  override def getBoundaryDimension: Int = polygon.getBoundaryDimension

  override def getCoordinate: Coordinate = polygon.getCoordinate

  override def isEmpty: Boolean = polygon.isEmpty

  override def normalize(): Unit = polygon.normalize()

  override def reverse(): Geometry = polygon.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = polygon.equalsExact(other, tolerance)

  override def getNumPoints: Int = polygon.getNumPoints

  override def reverseInternal(): Geometry = this

  override def copyInternal(): Geometry = this

  override def getTypeCode: Int = polygon.getTypeCode
}
