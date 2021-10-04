package it.agilelab.gis.core

import it.agilelab.gis.core.utils.WktConverter
import org.scalatest.{ FlatSpec, Matchers }

class GeometryDistanceSpec extends FlatSpec with Matchers {
  "Circle distance method" should "return the correct distance from a point" in {
    val wktStringCircle: String = "CIRCLE ((11.028014 45.33984), 200)"
    val wktStringPoint: String = "POINT(11.002371648547008 45.3416411637726)"
    val circle = WktConverter.converter(wktStringCircle)
    val point = WktConverter.converter(wktStringPoint)
    val expected = 1814
    circle.right.get.distance(point.right.get).toInt shouldEqual expected

  }

  it should "return zero distance from a point on the border" in {
    val wktStringCircle: String = "CIRCLE ((11.028014 45.33984), 200)"
    val wktStringPoint = "POINT(11.030031109783607 45.34094093409899)"
    val circle = WktConverter.converter(wktStringCircle)
    val point = WktConverter.converter(wktStringPoint)
    val expected = 0d
    circle.right.get.distance(point.right.get) shouldEqual expected

  }

  it should "return zero distance from a point because internal" in {
    val wktStringCircle: String = "CIRCLE((11.028014 45.33984), 200)"
    val wktStringPoint: String = "POINT(11.028797238084936 45.34044324524726)"
    val circle = WktConverter.converter(wktStringCircle)
    val point = WktConverter.converter(wktStringPoint)
    val expected = 0d
    circle.right.get.distance(point.right.get) shouldEqual expected

  }
}
