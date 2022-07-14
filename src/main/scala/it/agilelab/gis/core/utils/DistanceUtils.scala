package it.agilelab.gis.core.utils

object DistanceUtils {

  private val EARTH_RADIUS = 6372.8d // radius earth in km

  /** Calculate distance between two gps points in meters through `haversine` formula
    *
    * @param lat1 latitude of first point
    * @param lon1 longitude of first point
    * @param lat2 latitude of second point
    * @param lon2 longitude of second point
    * @return distance between two gps points
    */
  def haversineFormula(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double = {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLong = Math.toRadians(lon2 - lon1)
    val startLat = Math.toRadians(lat1)
    val endLat = Math.toRadians(lat2)
    val a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    EARTH_RADIUS * c * 1000
  }

  private def haversine(v: Double): Double = Math.pow(Math.sin(v / 2), 2)

}
