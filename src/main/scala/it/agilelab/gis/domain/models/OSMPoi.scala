package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.impl.CoordinateArraySequence
import com.vividsolutions.jts.geom._

abstract class OSMPoi(
    geometry: Geometry,
    osmId: Option[String] = None,
    name: Option[String] = None
) extends Geometry(geometry.getFactory) {

  override def toString: String =
    s"""Poi: ${geometry.toString}
       |osmId: ${osmId.getOrElse("")}
       |name: ${name.getOrElse("")}
       """.stripMargin

  /** As seen from class Street, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
  def apply(filter: CoordinateFilter): Unit = geometry.apply(filter)

  def apply(filter: CoordinateSequenceFilter): Unit = geometry.apply(filter)

  def apply(filter: GeometryFilter): Unit = geometry.apply(filter)

  def apply(filter: GeometryComponentFilter): Unit = geometry.apply(filter)

  override def computeEnvelopeInternal(): Envelope =
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

  override def isEmpty: Boolean = geometry.isEmpty

  override def getBoundary: Geometry = geometry.getBoundary

  override def compareToSameClass(o: scala.Any): Int = {
    val w: OSMPoi = o.asInstanceOf[OSMPoi]
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

  def getCoordinateSequence: CoordinateArraySequence =
    new CoordinateArraySequence(getCoordinates)

  override def getCoordinates: Array[Coordinate] = geometry.getCoordinates

  override def getNumPoints: Int = geometry.getNumPoints

  override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
    val w: OSMPoi = o.asInstanceOf[OSMPoi]
    comp.compare(getCoordinateSequence, w.getCoordinateSequence)
  }

  override def getCentroid: Point = geometry.getCentroid

  override def getDimension: Int = geometry.getDimension

  override def getGeometryType: String = geometry.getGeometryType

  override def getBoundaryDimension: Int = geometry.getBoundaryDimension

  override def getCoordinate: Coordinate = geometry.getCoordinate

  override def normalize(): Unit = geometry.normalize()

  override def reverse(): Geometry = geometry.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = geometry.equalsExact(other, tolerance)

}
