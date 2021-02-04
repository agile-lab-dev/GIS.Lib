package it.agilelab.bigdata.gis.domain.models

/**
 * @author Gloria Lovera
 */

import com.graphhopper.util.Helper
import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence

object OSMStreetAndHouseNumber {

  def decorateWithNumbers(roadEL: OSMStreetAndHouseNumber, addressEls: Seq[OSMAddress]): OSMStreetAndHouseNumber = {
    val numbersEls =
      addressEls.map(
        osmAddress =>
          OSMSmallAddressNumber(
            osmAddress.point.getCoordinate.x,
            osmAddress.point.getCoordinate.y,
            osmAddress.number)
      )

    roadEL.copy(numbers = numbersEls)
  }
}

case class OSMAddressNumber(point: Geometry, number: String)

case class OSMSmallAddressNumber(x: Double, y: Double, number: String)

case class OSMStreetAndHouseNumber(osm_id: String,
                                   pointsArray: Array[Double],
                                   street: Option[String],
                                   streetType: Option[OSMStreetType],
                                   numbers: Seq[OSMSmallAddressNumber],
                                   speedLimit: Option[Int],
                                   isBridge: Option[Boolean],
                                   isTunnel: Option[Boolean],
                                   oneway: Option[Boolean]
                            )
  extends Geometry(GeometryFactoryOSM.factory) {

  def getDistanceAndNumber(queryPoint: Point, threshold: Double): (Double, Option[String]) = {
    val number = numbers
      .map(a => {
        val distance = Helper.DIST_EARTH.calcDist(a.y, a.x, queryPoint.getY, queryPoint.getX)
        (distance, a.number)
      })
      .filter(_._1 <= threshold)
      .sortBy(_._1)
      .headOption

    if (number.isDefined)
      (number.get._1, Some(number.get._2))
    else //we return the minimum distance between the street and the query point and no number
      (getCoordinates.map(p1 => Helper.DIST_EARTH.calcDist(p1.y, p1.x, queryPoint.getY, queryPoint.getX)).min, None)
  }

  override def toString: String = {
    s"""
       |Line (LINE REMOVED)
       |Street: ${street.getOrElse("N.D")}
       |StreetType: ${streetType.map(_.value)}
       |Numbers: ${numbers.map(_.number).mkString(", ")}
       |SpeedLimit: ${speedLimit.map(_.toString).getOrElse("N.D")}
       |Oneway: ${oneway.getOrElse("N.D")}
       |isBridge: ${isBridge.map(_.toString).getOrElse("N.D")}
       |isTunnel: ${isTunnel.map(_.toString).getOrElse("N.D")}
    """.stripMargin
  }

  def getCoordinateSequence: CoordinateArraySequence = {
    new CoordinateArraySequence(getCoordinates)
  }

  override def computeEnvelopeInternal(): Envelope = {
    if (isEmpty)
      new Envelope
    else
      getCoordinateSequence.expandEnvelope(new Envelope)
  }

  override def getBoundary: Geometry = getBoundary

  override def compareToSameClass(o: scala.Any): Int = {
    val s: OSMStreetAndHouseNumber = o.asInstanceOf[OSMStreetAndHouseNumber]

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
    val s: OSMStreetAndHouseNumber = o.asInstanceOf[OSMStreetAndHouseNumber]
    comp.compare(getCoordinateSequence, s.getCoordinateSequence)
  }

  def getXIndex(n: Int): Int = n * dimensionPoints

  def getYIndex(n: Int): Int = n * dimensionPoints + 1

  def dimensionPoints = 2

  def getCoordinateN(n: Int): Coordinate = new Coordinate(pointsArray(getXIndex(n)), pointsArray(getYIndex(n)))

  def isClosed: Boolean = {
    if (this.isEmpty)
      false
    else
      this.getCoordinateN(0).equals2D(this.getCoordinateN(this.getNumPoints - 1))
  }

  def getLineString: LineString = factory.createLineString(pointsArray.sliding(2, 2).map(ll => new Coordinate(ll(0), ll(1))).toArray)


  override def getCoordinates: Array[Coordinate] = this.pointsArray.sliding(dimensionPoints, dimensionPoints).map(e => new Coordinate(e(0), e(1))).toArray

  override def getDimension: Int = 1

  override def getGeometryType: String = "LineString"

  override def getBoundaryDimension: Int = if (this.isClosed) -1 else 0

  override def getCoordinate: Coordinate = if (this.isEmpty) null else getCoordinateN(0)

  override def isEmpty: Boolean = this.pointsArray.length == 0

  override def normalize(): Unit = getLineString.normalize()

  override def reverse(): Geometry = getLineString.reverse()

  override def equalsExact(other: Geometry, tolerance: Double): Boolean = {
    if (!this.isEquivalentClass(other) || !other.isInstanceOf[OSMStreetAndHouseNumber]) false
    else {
      val otherLine = other.asInstanceOf[OSMStreetAndHouseNumber]
      if (this.pointsArray.length != otherLine.pointsArray.length) false
      else {
        this.getCoordinates.zip(otherLine.getCoordinates).forall(e => coordinateEqual(e._1, e._2, tolerance))
      }
    }
  }

  protected def coordinateEqual(a: Coordinate, b: Coordinate, tolerance: Double): Boolean = {
    if (tolerance == 0.0D) a == b
    else a.distance(b) <= tolerance
  }

  override def getNumPoints: Int = pointsArray.length / dimensionPoints

  def apply(filter: CoordinateFilter): Unit = getLineString.apply(filter)

  def apply(filter: CoordinateSequenceFilter): Unit = getLineString.apply(filter)

  def apply(filter: GeometryFilter): Unit = getLineString.apply(filter)

  def apply(filter: GeometryComponentFilter): Unit = getLineString.apply(filter)

}
