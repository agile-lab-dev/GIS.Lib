package it.agilelab.gis.domain.loader

import com.typesafe.config.{ Config, ConfigFactory }
import it.agilelab.gis.domain.managers.GeocodePathManager
import it.agilelab.gis.domain.models.OSMBoundary
import org.locationtech.jts.geom.{ Coordinate, GeometryFactory }
import org.scalatest.{ FeatureSpec, GivenWhenThen, Matchers }

import scala.collection.JavaConverters._

class ContainRelationBtwGeometriesSpec extends FeatureSpec with GivenWhenThen with Matchers {

  val coord_andorra_inside = new Coordinate(1.53402, 42.55658)
  val coord_andorra_outside0 = new Coordinate(1.66921, 42.50555)
  val coord_andorra_perimeter = new Coordinate(1.6690951, 42.5057918)
  val coord_vaticano_inside = new Coordinate(12.45467, 41.90221)
  val coord_toulouse = new Coordinate(1.648, 43.493)
  val gf = new GeometryFactory()
  val rootConf: Config = ConfigFactory.load()
  val geocodeConf: Config = rootConf.getConfig("geocode")
  val indexConf: Config = geocodeConf.getConfig("index")
  val inputPaths: Seq[String] = indexConf.getStringList("input_paths").asScala
  val boundaryConf: Config = indexConf.getConfig("boundary")
  val pathConf: Config = indexConf.getConfig("path")
  val pathManager: GeocodePathManager = GeocodePathManager(pathConf)
  val omsBoundariesSelector: String => List[OSMBoundary] = (s: String) =>
    OSMAdministrativeBoundariesLoader(boundaryConf, pathManager).loadObjects(s"${inputPaths.head}/$s")

  feature("Issue#14 Check if a Point is Inside a Polygon - State of the art") {

    scenario("a point is OUTSIDE a non-rectangular, concave polygon") {

      Given("Andorra country boundary")
      val andorraCountryBoundary = omsBoundariesSelector("andorra/andorra-AL2.shp").head

      And("a point is defined outside it")
      val aPoint = gf.createPoint(coord_andorra_outside0);

      Then("the 'covers' functionality exposed should verify it")
      andorraCountryBoundary.covers(aPoint) shouldBe false
    }

    scenario("a point is INSIDE a non-rectangular, concave polygon") {

      Given("Andorra country boundary")
      val andorraCountryBoundary = omsBoundariesSelector("andorra/andorra-AL2.shp").head

      And("a point is defined inside it")
      val aPoint = gf.createPoint(coord_andorra_inside);

      Then("the 'covers' functionality exposed should verify it")
      andorraCountryBoundary.covers(aPoint) shouldBe true
    }

    scenario("a point is PART OF THE PERIMETER of a non-rectangular, concave polygon") {
      Given("Andorra country boundary")
      val andorraCountryBoundary = omsBoundariesSelector("andorra/andorra-AL2.shp").head

      And("a point is defined inside it")
      val aPoint = gf.createPoint(coord_andorra_perimeter);

      Then("the 'covers' functionality exposed should verify it")
      andorraCountryBoundary.covers(aPoint) shouldBe true
      And(
        "the 'contains' functionality exposed should NOT verify it - it means the point resides on the border actually")
      andorraCountryBoundary.contains(aPoint) shouldBe false
    }

    scenario("a point is OUTSIDE, sitting in a 'hole' of a non-rectangular, concave polygon") {

      Given("Lazio region boundary")
      val lazioRegionBoundary = omsBoundariesSelector("italy/italy-AL4.shp").filter(_.region.contains("Lazio")).head

      And("a point is defined somewhere in Vaticano country, that's inside Lazio's perimeter, but not part of its area")
      val aPoint = gf.createPoint(coord_vaticano_inside);

      Then("the 'covers' functionality exposed should verify it")
      lazioRegionBoundary.covers(aPoint) shouldBe false
    }

  }

  feature("Issue#15 Check if a MultiPoint intersect a Polygon - State of the art") {

    scenario("a multipoint is outside of a non-rectangular, concave polygon completely") {

      Given("Andorra country boundary")
      val andorraCountryBoundary = omsBoundariesSelector("andorra/andorra-AL2.shp").head

      And("a 3-point line is defined outside it with neither intersections nor point inside it")
      val coord_andorra_outside1 = new Coordinate(1.7024, 42.4278)
      val coord_corse_inside = new Coordinate(9.3356, 42.2732)
      val points = gf.createMultiPoint(Seq(coord_andorra_outside0, coord_andorra_outside1, coord_corse_inside).toArray);

      Then("the 'intersect' functionality exposed should verify it")
      andorraCountryBoundary.intersects(points) shouldBe false
    }

    scenario("a multipoint intersect a non-rectangular, concave polygon - a point is inside it at least") {

      Given("Andorra country boundary")
      val andorraCountryBoundary = omsBoundariesSelector("andorra/andorra-AL2.shp").head

      And("a 3-point line intersect it, but with a points inside it at least")
      val points = gf.createMultiPoint(Seq(coord_andorra_outside0, coord_andorra_inside, coord_toulouse).toArray);

      Then("the 'intersect' functionality exposed should verify it")
      andorraCountryBoundary.intersects(points) shouldBe true
    }

  }

}
