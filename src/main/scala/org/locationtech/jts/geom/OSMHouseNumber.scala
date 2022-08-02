package org.locationtech.jts.geom

import org.locationtech.jts.geom.impl.CoordinateArraySequence

case class OSMHouseNumber(point: Geometry, number: String) extends Geometry(point.getFactory) {

  setUserData(this)

  /** As seen from class Street, the missing signatures are as follows.
    * For convenience, these are usable as stub implementations.
    */
  def apply(filter: CoordinateFilter): Unit = point.apply(filter)

  def apply(filter: CoordinateSequenceFilter): Unit = point.apply(filter)

  def apply(filter: GeometryFilter): Unit = point.apply(filter)

  def apply(filter: GeometryComponentFilter): Unit = point.apply(filter)

  override def computeEnvelopeInternal(): Envelope =
    if (isEmpty) {
      new Envelope
    } else {
      getCoordinateSequence.expandEnvelope(new Envelope)
    }

  override def isEmpty: Boolean = point.isEmpty

  override def getBoundary: Geometry = point.getBoundary

  override def compareToSameClass(o: Any): Int =
    getCoordinate.compareTo(o.asInstanceOf[OSMHouseNumber].getCoordinate)

  override def getCoordinate: Coordinate = point.getCoordinate

  override def compareToSameClass(o: Any, comp: CoordinateSequenceComparator): Int =
    comp.compare(getCoordinateSequence, o.asInstanceOf[OSMHouseNumber].getCoordinateSequence)

  def getCoordinateSequence: CoordinateArraySequence =
    new CoordinateArraySequence(getCoordinates)

  override def getCoordinates: Array[Coordinate] = point.getCoordinates

  override def getDimension: Int = point.getDimension

  override def getGeometryType: String = point.getGeometryType

  override def getBoundaryDimension: Int = point.getBoundaryDimension

  override def normalize(): Unit = point.normalize()

  override def reverse(): Geometry = point.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = point.equalsExact(other, tolerance)

  override def getNumPoints: Int = 1

  override def toString: String =
    s"""Point:${point.toString}
       |Number:$number
       """.stripMargin

  override def reverseInternal(): Geometry = point.reverseInternal()

  override def copyInternal(): Geometry = point.copyInternal()

  override def getTypeCode: Int = point.getTypeCode
}
