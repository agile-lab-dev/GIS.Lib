package it.agilelab.bigdata.gis.domain.models

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence

object OSMStreet {}

case class OSMStreet(multiLineString: Geometry,
                     street: Option[String],
                     code: Option[String],
                     isBridge: Option[Boolean],
                     isTunnel: Option[Boolean],
                     speedLimit: Option[Int],
                     bidirected: Option[Boolean],
                     streetType: Option[OSMStreetType])
  extends MultiLineString(
    {
      val length = multiLineString.getNumGeometries
      (0 until length).map {
        x => multiLineString.getGeometryN(x).asInstanceOf[LineString]
      }
    }.toArray,
    multiLineString.getFactory
  ) {

  override def toString: String = {
    s"""
       |Line: ${multiLineString.toString}
       |Street: ${street.map(_.toString)}
       |SpeedLimit: ${speedLimit.map(_.toString)}
       |Bidirected: ${bidirected.map(_.toString)}
       |StreetType: ${streetType.map(_.value)}
       |code: ${code.map(_.toString)}
       |isBridge: ${isBridge.map(_.toString)}
       |isTunnel: ${isTunnel.map(_.toString)}
    """.stripMargin
  }

  def getCoordinateSequence: CoordinateArraySequence = {
    new CoordinateArraySequence(getCoordinates)
  }

  override def computeEnvelopeInternal(): Envelope = {
    if (isEmpty) new Envelope
    else getCoordinateSequence.expandEnvelope(new Envelope)

  }

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

  override def reverse(): Geometry = multiLineString.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = multiLineString.equalsExact(other, tolerance)

  override def getNumPoints: Int = multiLineString.getNumPoints

  def isForCar: Boolean = !isNotForCar

  def isNotForCar: Boolean = streetType.exists {
    case OSMStreetType.CYCLEWAY => true
    case _ => isForPedestrian
  }

  def isForPedestrian: Boolean = streetType.exists {
    case OSMStreetType.PEDESTRIAN | OSMStreetType.FOOTWAY | OSMStreetType.STEPS => true
    case _ => false
  }

}