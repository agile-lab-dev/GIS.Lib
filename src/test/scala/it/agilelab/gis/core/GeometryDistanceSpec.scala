package it.agilelab.gis.core

import it.agilelab.gis.core.utils.WktConverter
import org.scalatest.{ FlatSpec, Matchers }

class GeometryDistanceSpec extends FlatSpec with Matchers {
  "Circle distance method" should "return the correct distance from a point" in {
    val wktStringCircle: String = "CIRCLE((100 100),50)"
    val wktStringPoint: String = "POINT(0 100)"
    val circle = WktConverter.converter(wktStringCircle)
    val point = WktConverter.converter(wktStringPoint)
    val expected = 50d
    circle.right.get.distance(point.right.get) shouldEqual expected

  }

  it should "return zero distance from a point on the border" in {
    val wktStringCircle: String = "CIRCLE((100 100),50)"
    val wktStringPoint = "POINT(50 100)"
    val circle = WktConverter.converter(wktStringCircle)
    val point = WktConverter.converter(wktStringPoint)
    val expected = 0d
    circle.right.get.distance(point.right.get) shouldEqual expected

  }

  it should "return zero distance from a point because internal" in {
    val wktStringCircle: String = "CIRCLE((100 100),50)"
    val wktStringPoint: String = "POINT(70 100)"
    val circle = WktConverter.converter(wktStringCircle)
    val point = WktConverter.converter(wktStringPoint)
    val expected = 0d
    circle.right.get.distance(point.right.get) shouldEqual expected

  }
}
