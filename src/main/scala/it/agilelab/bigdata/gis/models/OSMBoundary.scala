package it.agilelab.bigdata.gis.models

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence

case class OSMBoundary(polygon: Geometry, name: String) extends Geometry(polygon.getFactory) {

  override def toString() = {
    s"""Line:${polygon.toString()}
       |Name:$name
       """.stripMargin
  }

  /** As seen from class Street, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
  def apply(filter: CoordinateFilter) = polygon.apply(filter)

  def apply(filter: CoordinateSequenceFilter)  = polygon.apply(filter)

  def apply(filter: GeometryFilter) = polygon.apply(filter)

  def apply(filter: GeometryComponentFilter) = polygon.apply(filter)

  def getCoordinateSequence() = {
    new CoordinateArraySequence(getCoordinates)
  }

  override def computeEnvelopeInternal(): Envelope = {
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

  }

  override def getBoundary: Geometry = polygon.getBoundary

  override def compareToSameClass(o: scala.Any): Int = {
    val s: OSMBoundary = o.asInstanceOf[OSMBoundary]
    // MD - optimized implementation
    var i: Int = 0
    var j: Int = 0
    while (i < getNumPoints && j < s.getNumPoints) {
      val comparison: Int = getCoordinateSequence.getCoordinate(i).compareTo(s.getCoordinateSequence.getCoordinate(j))
      if (comparison != 0) return comparison
      i += 1
      j += 1
    }
    if (i < getNumPoints) {
      return 1
    }
    if (j < s.getNumPoints) {
      return -1
    }
    return 0
  }

  override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
    val s: OSMStreet = o.asInstanceOf[OSMStreet]
    return comp.compare(getCoordinateSequence(), s.getCoordinateSequence())
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


}
