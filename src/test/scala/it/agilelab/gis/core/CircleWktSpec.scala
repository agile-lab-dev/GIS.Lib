package it.agilelab.gis.core

import it.agilelab.gis.core.utils.{ DistanceUtils, WktConverter }
import org.locationtech.jts.geom.{ Coordinate, GeometryFactory }
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.prop.TableDrivenPropertyChecks

/** Tests that build a CIRCLE geometry from a WKT string  and verify
 *  whether a given point falls inside or outside the circle.
 *
 * Example of WKT circle:
 *   CIRCLE ((17.083214626953133 41.04442611163382), 28000)
 * where the first pair of coordinates is the center (longitude latitude)
 * and the second value is the radius (in meters).
 */
class CircleWktSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  private val geometryFactory = new GeometryFactory()

  private val centerLongitude = 17.083214626953133
  private val centerLatitude = 41.04442611163382
  private val radiusInMeters = 28000

  private val circleWkt = s"CIRCLE (($centerLongitude $centerLatitude), $radiusInMeters)"

  private def isPointInsideCircle(wkt: String, longitude: Double, latitude: Double): Boolean = {
    val geometry = WktConverter.converter(wkt) match {
      case Right(geom) => geom
      case Left(error) => fail(s"Unable to parse WKT '$wkt': $error")
    }
    val point = geometryFactory.createPoint(new Coordinate(longitude, latitude))
    geometry.contains(point)
  }

  /** Distance used by Circle.contains (same coordinate order and formula), in meters. */
  private def circleDistanceInMeters(
                                      lon1: Double,
                                      lat1: Double,
                                      lon2: Double,
                                      lat2: Double
                                    ): Double =
    DistanceUtils.haversineFormula(lat1, lon1, lat2, lon2)


  private val pointsToVerify = Table(
    ("longitude", "latitude", "expectedInside"),
    (17.08321, 41.22424, true),
    (17.41430, 41.18780, false),
    (17.28941, 40.95434, true),
    (17.08321, 40.75672, false),
    (16.87702, 40.95434, true),
    (16.75213, 41.18780, false)
  )

  "The given set of points" should "be correctly classified as inside or outside the circle" in {
    forAll(pointsToVerify) { (longitude, latitude, expectedInside) =>
      val distanceMeters = circleDistanceInMeters(centerLongitude, centerLatitude, longitude, latitude)
      val actualInside = isPointInsideCircle(circleWkt, longitude, latitude)
      val inOutLabel = if (actualInside) "IN" else "OUT"
      // scalastyle:off
      println(
        f"point ($longitude%.5f, $latitude%.5f) -> distance from center: $distanceMeters%.2f m (radius: $radiusInMeters m) -> $inOutLabel"
      )
      // scalastyle:on
      withClue(s"point ($longitude, $latitude): ") {
        actualInside shouldBe expectedInside
      }
    }
  }
}
