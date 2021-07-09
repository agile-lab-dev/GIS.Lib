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

/** TracePoint is matched point on a road.
  * @param latitude         input latitude
  * @param longitude        input longitude
  * @param altitude         input altitude
  * @param time             input time
  * @param matchedLatitude  matched latitude
  * @param matchedLongitude matched longitude
  * @param matchedAltitude  matched altitude
  * @param roadType         road type
  * @param roadName         road name
  * @param speedLimit       speed limit on the road with name [[roadName]]
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

/** DistancePoint holds data of a path between two [[TracePoint]]s.
  * @param node1 trace point 1
  * @param node2 trace point 2
  * @param distance distance between [[node1]] and [[node2]]
  * @param diffTime difference of time between [[node1]] and [[node2]]
  * @param typeOfRoute type of route
  */
case class DistancePoint(
    node1: TracePoint,
    node2: TracePoint,
    distance: Option[Double],
    diffTime: Long,
    typeOfRoute: Option[String]
)

/** MatchedRoutes holds data of a matched route.
  * @param points trace points
  * @param length length of the entire route, it's roughly equal to the sum of every distance in [[distanceBetweenPoints]].
  * @param time the time it takes to run across the route.
  * @param routes routes
  * @param distanceBetweenPoints distance between consecutive point in [[points]]
  *                              Usually |[[points]]| == |[[distanceBetweenPoints]]+1|
  */
case class MatchedRoute(
    points: Seq[TracePoint],
    length: Option[Double],
    time: Option[Long],
    routes: Map[String, Double],
    distanceBetweenPoints: Seq[DistancePoint]
) {

  def getKmType(s: String): Try[Double] =
    Try.apply(routes(s))

}
