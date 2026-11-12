package it.agilelab.gis.core

import it.agilelab.gis.core.model.geometry.{ Circle, GeometryFactoryEnriched }
import it.agilelab.gis.core.utils.DistanceUtils
import org.locationtech.jts.geom.{ Coordinate, GeometryFactory }
import org.scalatest.{ FlatSpec, Matchers }

/** Verifies that [[Circle.distance]] correctly computes the great-circle distance
  * from the circle's centroid, using longitude as X and latitude as Y
  * (i.e. `center.getX` must be the longitude and `center.getY` must be the latitude,
  * and they must NOT be swapped when passed to the haversine formula).
  *
  * The test exploits the fact that, away from the equator, one degree of longitude
  * and one degree of latitude correspond to different distances on the ground.
  * If `getX`/`getY` were inverted, the computed distance for an east-west offset
  * would match the north-south expected distance (and vice versa), which the
  * assertions below would catch.
  */
class CircleDistanceSpec extends FlatSpec with Matchers {

  private val geometryFactory = new GeometryFactory()
  private val circleFactory = new GeometryFactoryEnriched()

  // Milan, Italy - chosen because it's far enough from the equator that
  // 1 degree of longitude != 1 degree of latitude in meters.
  private val centerLongitude = 9.19000
  private val centerLatitude = 45.4642

  private val offsetDegrees = 0.1
  private val radiusInMeters = 1000.0

  private def buildCircle(radius: Double): Circle = {
    val centerPoint = geometryFactory.createPoint(new Coordinate(centerLongitude, centerLatitude))
    Circle(centerPoint, radius, circleFactory)
  }

  private def buildPoint(longitude: Double, latitude: Double) =
    geometryFactory.createPoint(new Coordinate(longitude, latitude))

  "Circle.distance" should "correctly compute the distance to a point offset only in longitude (east)" in {
    val eastPoint = buildPoint(centerLongitude + offsetDegrees, centerLatitude)

    val expectedRawDistance =
      DistanceUtils.haversineFormula(centerLatitude, centerLongitude, centerLatitude, centerLongitude + offsetDegrees)
    val expectedDistance = expectedRawDistance - radiusInMeters

    val circle = buildCircle(radiusInMeters)
    val actualDistance = circle.distance(eastPoint)

    actualDistance shouldBe expectedDistance +- 0.01
  }

  it should "correctly compute the distance to a point offset only in latitude (north)" in {
    val northPoint = buildPoint(centerLongitude, centerLatitude + offsetDegrees)

    val expectedRawDistance =
      DistanceUtils.haversineFormula(centerLatitude, centerLongitude, centerLatitude + offsetDegrees, centerLongitude)
    val expectedDistance = expectedRawDistance - radiusInMeters

    val circle = buildCircle(radiusInMeters)
    val actualDistance = circle.distance(northPoint)

    actualDistance shouldBe expectedDistance +- 0.01
  }

  it should "produce different distances for east and north offsets, proving X/Y are not swapped" in {
    // At this latitude, 1 degree of longitude is notably shorter than 1 degree of latitude
    // in meters. If getX/getY were swapped, both distances would come out equal
    // (since the same offset would be interpreted identically in both cases).
    val eastPoint = buildPoint(centerLongitude + offsetDegrees, centerLatitude)
    val northPoint = buildPoint(centerLongitude, centerLatitude + offsetDegrees)

    val circle = buildCircle(radiusInMeters)

    val eastDistance = circle.distance(eastPoint)
    val northDistance = circle.distance(northPoint)

    eastDistance should not be (northDistance +- 0.01)
    eastDistance should be < northDistance
  }

}

