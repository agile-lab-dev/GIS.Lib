package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory}
import it.agilelab.bigdata.gis.domain.managers.PathManager
import it.agilelab.bigdata.gis.domain.models.OSMBoundary
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class ContainmentRelationBtwGeometriesSpec
    extends FeatureSpec
    with GivenWhenThen
    with Matchers {

  feature("Check if a Point is Inside a Polygon") {

    import Utils._

    scenario("a point is OUTSIDE a non-rectangular, concave polygon") {

      Given("Andorra country boundary")
      val andorraCountryBoundary =
        omsBoundariesSelector("andorra/Andorra_AL2.shp").head
      //val _shapeReader = shapesReader("andorra/Andorra_AL2.shp").head

      And("a point is defined outside it")
      val aPoint = gf.createPoint(new Coordinate(1.66921, 42.50555))

      Then("the 'covers' functionality exposed should verify it")
      andorraCountryBoundary.covers(aPoint) shouldBe false
    }

    scenario("a point is INSIDE a non-rectangular, concave polygon") {

      Given("Andorra country boundary")
      val andorraCountryBoundary =
        omsBoundariesSelector("andorra/Andorra_AL2.shp").head

      And("a point is defined inside it")
      val aPoint = gf.createPoint(new Coordinate(1.53402, 42.55658))

      Then("the 'covers' functionality exposed should verify it")
      andorraCountryBoundary.covers(aPoint) shouldBe true
    }

    scenario(
      "a point is PART OF THE PERIMETER of a non-rectangular, concave polygon"
    ) {
      Given("Andorra country boundary")
      val andorraCountryBoundary =
        omsBoundariesSelector("andorra/Andorra_AL2.shp").head

      And("a point is defined inside it")
      val aPoint = gf.createPoint(new Coordinate(1.6690951, 42.5057918))

      Then("the 'covers' functionality exposed should verify it")
      andorraCountryBoundary.covers(aPoint) shouldBe true
      And(
        "the 'contains' functionality exposed should NOT verify it - it means the point resides on the border actually"
      )
      andorraCountryBoundary.contains(aPoint) shouldBe false
    }

    scenario(
      "a point is OUTSIDE, sitting in a 'hole' of a non-rectangular, concave polygon"
    ) {

      Given("Lazio region boundary")
      val lazioRegionBoundary = omsBoundariesSelector("italy/Italy_AL4.shp")
        .filter(_.region.contains("Lazio"))
        .head

      And(
        "a point is defined somewhere in Vaticano country, that's inside Lazio's perimeter, but not part of its area"
      )
      val aPoint = gf.createPoint(new Coordinate(12.45467, 41.90221))

      Then("the 'covers' functionality exposed should verify it")
      lazioRegionBoundary.covers(aPoint) shouldBe false
    }

  }

  object Utils {
    val omsBoundariesSelector: String => List[OSMBoundary] = (s: String) =>
      new OSMAdministrativeBoundariesLoader().loadObjects(s"${PathManager.getInputPath}/$s")
    val gf = new GeometryFactory()
  }

}
