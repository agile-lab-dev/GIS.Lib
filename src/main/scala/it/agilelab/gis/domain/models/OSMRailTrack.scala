package it.agilelab.gis.domain.models

import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.locationtech.jts.geom.{
  Coordinate,
  CoordinateSequenceComparator,
  Envelope,
  Geometry,
  LineString,
  MultiLineString
}

case class OSMRailTrack(
    multiLineString: Geometry,
    osmId: Option[String] = None,
    railway: Option[String] = None,
    railwayType: Option[OSMRailwayType] = None,
    operator: Option[String] = None,
    usage: Option[OSMUsage] = None
) extends MultiLineString(
      {
        val length = multiLineString.getNumGeometries
        (0 until length).map { x =>
          multiLineString.getGeometryN(x).asInstanceOf[LineString]
        }
      }.toArray,
      multiLineString.getFactory
    ) {

  override def toString: String =
    s"""
       |Line: ${multiLineString.toString}
       |Name: ${railway.map(_.toString)}
       |Type: ${railwayType.map(_.value)}
       |Operator: ${operator.map(_.toString)}
       |Usage: ${usage.map(_.value)}
    """.stripMargin

  def getCoordinateSequence: CoordinateArraySequence =
    new CoordinateArraySequence(getCoordinates)

  override def computeEnvelopeInternal(): Envelope =
    if (isEmpty) new Envelope
    else getCoordinateSequence.expandEnvelope(new Envelope)

  override def getBoundary: Geometry = multiLineString.getBoundary

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
    if (i < getNumPoints) 1
    else if (j < s.getNumPoints) -1
    else 0
  }

  override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
    val s: OSMStreet = o.asInstanceOf[OSMStreet]
    comp.compare(getCoordinateSequence, s.getCoordinateSequence)
  }

  override def getCoordinates: Array[Coordinate] = multiLineString.getCoordinates

  override def getDimension: Int = multiLineString.getDimension

  override def getGeometryType: String = multiLineString.getGeometryType

  override def getBoundaryDimension: Int = multiLineString.getBoundaryDimension

  override def getCoordinate: Coordinate = multiLineString.getCoordinate

  override def isEmpty: Boolean = multiLineString.isEmpty

  override def normalize(): Unit = multiLineString.normalize()

}
