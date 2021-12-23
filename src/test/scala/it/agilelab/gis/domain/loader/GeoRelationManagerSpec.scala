package it.agilelab.gis.domain.loader

import com.typesafe.config.{ Config, ConfigFactory }
import it.agilelab.gis.core.utils.ConfigurationProperties.GEORELATION
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.managers.GeoRelationManager
import org.scalatest.{ BeforeAndAfterAll, EitherValues, FlatSpec, Matchers }

class GeoRelationManagerSpec extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  val conf: Config = ConfigFactory.load().getConfig(GEORELATION.value)
  val geoRelationManager: GeoRelationManager = GeoRelationManager(conf)

  "Railways index" should "be used to compute distance from a point in Italy" in {
    val point1: IdentifiableGPSPoint = IdentifiableGPSPoint("point1", 44.0150, 8.6197, None, System.currentTimeMillis())
    val point2: IdentifiableGPSPoint = IdentifiableGPSPoint("point2", 44.1338, 9.0669, None, System.currentTimeMillis())

    val disPoint1 = 32422
    val disPoint2 = 25820

    val response1 = geoRelationManager.nearestRailway(point1).right.value
    val response2 = geoRelationManager.nearestRailway(point2).right.value

    val distance1 = response1.distance
    val distance2 = response2.distance

    distance1.map(_.round) shouldBe Some(disPoint1)
    distance2.map(_.round) shouldBe Some(disPoint2)
  }

  "Sea index" should "be used to check if the point is inside the sea" in {
    val point3 = IdentifiableGPSPoint("inSea", 41.68147, 12.15817, None, System.currentTimeMillis())
    val point4 = IdentifiableGPSPoint("outSea", 41.7612, 12.3296, None, System.currentTimeMillis())

    val checkPoint3: Boolean = true
    val checkPoint4: Boolean = false

    val response3 = geoRelationManager.isInsideSea(point3).right.value
    val response4 = geoRelationManager.isInsideSea(point4).right.value

    response3.isInside shouldBe Some(checkPoint3)
    response4.isInside shouldBe Some(checkPoint4)
  }

  "Railways index" should "be used to compute distance from a point in Greece" in {
    val point5 = IdentifiableGPSPoint("point5", 39.0950, 22.1029, None, System.currentTimeMillis()) // inside Greece

    val response5 = geoRelationManager.nearestRailway(point5).right.value

    response5.distance.map(_.round) shouldBe Some(7305)
  }
}
