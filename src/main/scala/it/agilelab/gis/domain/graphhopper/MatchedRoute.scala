package it.agilelab.gis.domain.graphhopper

import com.graphhopper.util.GPXEntry

import scala.util.Try

/** @param lat  input latitude
  * @param lon  input longitude
  * @param alt  input altitude
  * @param time input time
  */
class GPSPoint(val lat: Double, val lon: Double, val alt: Option[Double], val time: Long) {

  def toTracePoint: TracePoint =
    TracePoint(
      latitude = lat,
      longitude = lon,
      altitude = alt,
      time = time,
      matchedLatitude = None,
      matchedLongitude = None,
      matchedAltitude = None,
      linearDistance = None,
      roadType = None,
      roadName = None,
      speedLimit = None
    )

  def toGPXEntry: GPXEntry = new GPXEntry(lat, lon, time)

}

object GPSPoint {

  def apply(lat: Double, lon: Double, alt: Option[Double], time: Long) = new GPSPoint(lat, lon, alt, time)
}

/** IdentifiableGPSPoint is a GPSPoint with an id.
  *
  * @param id   GPS point id
  * @param lat  input latitude
  * @param lon  input longitude
  * @param alt  input altitude
  * @param time input time
  */
case class IdentifiableGPSPoint(
    id: String,
    override val lat: Double,
    override val lon: Double,
    override val alt: Option[Double],
    override val time: Long
) extends GPSPoint(lat, lon, alt, time)

/** @param latitude         input latitude
  * @param longitude        input longitude
  * @param altitude         input altitude
  * @param time             input time, optional
  * @param matchedLatitude  matched latitude
  * @param matchedLongitude matched longitude
  * @param matchedAltitude  matched altitude
  * @param linearDistance   distance between input point and map matched point
  */
case class TracePoint(
    latitude: Double,
    longitude: Double,
    altitude: Option[Double],
    time: Long,
    matchedLatitude: Option[Double],
    matchedLongitude: Option[Double],
    matchedAltitude: Option[Double],
    roadType: Option[String],
    roadName: Option[String],
    speedLimit: Option[Int],
    linearDistance: Option[Double]
)

case class DistancePoint(node1: TracePoint, node2: TracePoint, distance: Double, diffTime: Long, typeOfRoute: String)

case class MatchedRoute(
    points: Seq[TracePoint],
    length: Double,
    time: Long,
    routes: Map[String, Double],
    distanceBetweenPoints: Seq[DistancePoint]
) {

  def getKmType(s: String): Try[Double] =
    Try.apply(routes(s))

}
