package it.agilelab.bigdata.gis.models

import com.vividsolutions.jts.geom.impl.CoordinateArraySequence
import com.vividsolutions.jts.geom._

class GeometryWrapper[T <: Geometry](id: String, geometry: T) extends Geometry(geometry.getFactory) {


  /** As seen from class Street, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
  def apply(filter: CoordinateFilter) = geometry.apply(filter)

  def apply(filter: CoordinateSequenceFilter)  = geometry.apply(filter)

  def apply(filter: GeometryFilter) = geometry.apply(filter)

  def apply(filter: GeometryComponentFilter) = geometry.apply(filter)

  def getCoordinateSequence() = {
    new CoordinateArraySequence(getCoordinates)
  }

  override def computeEnvelopeInternal(): Envelope = {
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

  }

  override def getBoundary: Geometry = geometry.getBoundary

  override def compareToSameClass(o: scala.Any): Int = {
    val s: OSMStreet = o.asInstanceOf[OSMStreet]
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

  override def getCoordinates: Array[Coordinate] = geometry.getCoordinates

  override def getDimension: Int = geometry.getDimension

  override def getGeometryType: String = geometry.getGeometryType

  override def getBoundaryDimension: Int = geometry.getBoundaryDimension

  override def getCoordinate: Coordinate = geometry.getCoordinate

  override def isEmpty: Boolean = geometry.isEmpty

  override def normalize(): Unit = geometry.normalize()

  override def reverse(): Geometry = geometry.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = geometry.equalsExact(other, tolerance)

  override def getNumPoints: Int = geometry.getNumPoints



}
