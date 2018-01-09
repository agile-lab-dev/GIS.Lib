package it.agilelab.bigdata.gis.models

import javax.swing.plaf.synth.Region

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence

case class OSMBoundary(multiPolygon: Geometry,
                       city: Option[String],
                       county: Option[String],
                       region: Option[String],
                       country: Option[String],
                       boundaryType: String)
  extends MultiPolygon(
    {
      val length = multiPolygon.getNumGeometries
      (0 until length).map{
        x => multiPolygon.getGeometryN(x).asInstanceOf[Polygon]
      }
    }.toArray,
    multiPolygon.getFactory
  ) {

  override def toString() = {
    s"""Line: ${multiPolygon.toString}
       |City: ${city.map(_.toString)}
       |County: ${county.map(_.toString)}
       |Region: ${region.map(_.toString)}
       |Country: ${country.map(_.toString)}
       """.stripMargin
  }


  def isAddressDefined = city.isDefined && county.isDefined && region.isDefined && country.isDefined

  /** As seen from class Street, the missing signatures are as follows.
    *  For convenience, these are usable as stub implementations.
    */
//  def apply(filter: CoordinateFilter) = multiPolygon.apply(filter)
//
//  def apply(filter: CoordinateSequenceFilter)  = multiPolygon.apply(filter)
//
//  def apply(filter: GeometryFilter) = multiPolygon.apply(filter)
//
//  def apply(filter: GeometryComponentFilter) = multiPolygon.apply(filter)

  def getCoordinateSequence() = {
    new CoordinateArraySequence(getCoordinates)
  }

  override def computeEnvelopeInternal(): Envelope = {
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

  }

  override def getBoundary: Geometry = multiPolygon.getBoundary

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

  override def getCoordinates: Array[Coordinate] = multiPolygon.getCoordinates

  override def getDimension: Int = multiPolygon.getDimension

  override def getGeometryType: String = multiPolygon.getGeometryType

  override def getBoundaryDimension: Int = multiPolygon.getBoundaryDimension

  override def getCoordinate: Coordinate = multiPolygon.getCoordinate

  override def isEmpty: Boolean = multiPolygon.isEmpty

  override def normalize(): Unit = multiPolygon.normalize()

  override def reverse(): Geometry = multiPolygon.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = multiPolygon.equalsExact(other, tolerance)

  override def getNumPoints: Int = multiPolygon.getNumPoints


}
