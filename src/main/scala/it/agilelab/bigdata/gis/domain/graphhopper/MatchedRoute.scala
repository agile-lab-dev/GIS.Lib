package it.agilelab.bigdata.gis.domain.graphhopper

import com.graphhopper.util.GPXEntry

import scala.util.Try

/**
 *
 * @param lat  input latitude
 * @param lon  input longitude
 * @param alt  input altitude
 * @param time input time
 */
case class GPSPoint(lat: Double, lon: Double, alt: Option[Double], time: Long) {

  def toTracePoint: TracePoint = {
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
      speedLimit = None)
  }

  def toGPXEntry: GPXEntry = new GPXEntry(lat, lon, time)

}

/**
 *
 * @param latitude         input latitude
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

case class MatchedRoute(points: Seq[TracePoint],
                        length: Double,
                        time: Long,
                        routes: Map[String, Double],
                        distanceBetweenPoints: Seq[DistancePoint]) {

  def getKmType(s: String): Try[Double] = {
    Try.apply(routes(s))
  }

}
