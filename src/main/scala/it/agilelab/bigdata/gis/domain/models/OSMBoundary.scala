package it.agilelab.bigdata.gis.domain.models

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence

case class OSMBoundary(
    multiPolygon: Geometry,
    city: Option[String] = None,
    county: Option[String] = None,
    region: Option[String] = None,
    country: Option[String] = None,
    countryCode: Option[String] = None,
    countyCode: Option[String] = None,
    postalCode: Option[String] = None,
    boundaryType: String,
    env: Envelope
) extends MultiPolygon(
      {
        val length = multiPolygon.getNumGeometries
        (0 until length).map { x =>
          multiPolygon.getGeometryN(x).asInstanceOf[Polygon]
        }
      }.toArray,
      multiPolygon.getFactory
    ) {

  override def toString: String =
    s"""Line: ${multiPolygon.toString}
       |City: ${city.map(_.toString)}
       |County: ${county.map(_.toString)}
       |Region: ${region.map(_.toString)}
       |Country: ${country.map(_.toString)}
       |PostalCode: ${postalCode.map(_.toString)}
       """.stripMargin

  def isAddressDefined: Boolean =
    city.isDefined && county.isDefined && region.isDefined && country.isDefined

  /** As seen from class Street, the missing signatures are as follows.
    * For convenience, these are usable as stub implementations.
    */
  //  def apply(filter: CoordinateFilter) = multiPolygon.apply(filter)
  //
  //  def apply(filter: CoordinateSequenceFilter)  = multiPolygon.apply(filter)
  //
  //  def apply(filter: GeometryFilter) = multiPolygon.apply(filter)
  //
  //  def apply(filter: GeometryComponentFilter) = multiPolygon.apply(filter)

  def getCoordinateSequence: CoordinateArraySequence =
    new CoordinateArraySequence(getCoordinates)

  override def computeEnvelopeInternal(): Envelope =
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)

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

    if (i < getNumPoints) 1
    else if (j < s.getNumPoints) -1
    else 0
  }

  override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
    val s: OSMStreet = o.asInstanceOf[OSMStreet]
    comp.compare(getCoordinateSequence, s.getCoordinateSequence)
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

  def customCovers(other: OSMBoundary): Boolean =
    other.env.getMinX >= this.env.getMinX &&
    other.env.getMaxX <= this.env.getMaxX &&
    other.env.getMinY >= this.env.getMinY &&
    other.env.getMaxY <= this.env.getMaxY

  /** Merges the current boundary with another one.
    * Only if an attribute is missing in the current boundary but defined in the other one its value will be updated.
    */
  def merge(other: OSMBoundary): OSMBoundary =
    this.copy(
      city = emptyToNone(this.city).orElse(emptyToNone(other.city)),
      county = emptyToNone(this.county).orElse(emptyToNone(other.county)),
      region = emptyToNone(this.region).orElse(emptyToNone(other.region)),
      country = emptyToNone(this.country).orElse(emptyToNone(other.country)),
      countryCode = emptyToNone(this.countryCode).orElse(emptyToNone(other.countryCode)),
      countyCode = emptyToNone(this.countyCode).orElse(emptyToNone(other.countyCode))
    )

  def emptyToNone(s: Option[String]): Option[String] =
    s match {
      case Some(s) => if (s.trim.isEmpty) None else Some(s.trim)
      case None    => None
    }
}
