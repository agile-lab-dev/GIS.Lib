package it.agilelab.bigdata.gis.domain.models

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence


case class OSMAddress(point: Geometry, street: String, number: String) extends Geometry(point.getFactory) {


  setUserData(this)

  /** As seen from class Street, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
  def apply(filter: CoordinateFilter) = point.apply(filter)

  def apply(filter: CoordinateSequenceFilter)  = point.apply(filter)

  def apply(filter: GeometryFilter) = point.apply(filter)

  def apply(filter: GeometryComponentFilter) = point.apply(filter)

  def getCoordinateSequence() = {
    new CoordinateArraySequence(getCoordinates)
  }

  override def computeEnvelopeInternal(): Envelope = {
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

  }

  override def getBoundary: Geometry = point.getBoundary

  override def compareToSameClass(o: scala.Any): Int = {
    val s: OSMAddress = o.asInstanceOf[OSMAddress]
    getCoordinate.compareTo(s.getCoordinate)
  }

  override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
    val s: OSMAddress = o.asInstanceOf[OSMAddress]
    return comp.compare(getCoordinateSequence(), s.getCoordinateSequence())
  }

  override def getCoordinates: Array[Coordinate] = point.getCoordinates

  override def getDimension: Int = point.getDimension

  override def getGeometryType: String = point.getGeometryType

  override def getBoundaryDimension: Int = point.getBoundaryDimension

  override def getCoordinate: Coordinate = point.getCoordinate

  override def isEmpty: Boolean = point.isEmpty

  override def normalize(): Unit = point.normalize()

  override def reverse(): Geometry = point.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = point.equalsExact(other, tolerance)

  override def getNumPoints: Int = 1

  override def toString() = {
    s"""Point:${point.toString()}
       |Street:$street
       |Number:$number
       """.stripMargin
  }

}
