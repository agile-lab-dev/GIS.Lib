package it.agilelab.gis.domain.model

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory, LinearRing, Point, Polygon }
import it.agilelab.gis.domain.models.GeometriesOperations
import org.scalatest.{ FlatSpec, Matchers }

class GeometriesOperationsSuite extends FlatSpec with Matchers {

  val geometryFactory = new GeometryFactory()

  "The intersection" should "be return a list of points inside the interseption of two geometries" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val ring1: LinearRing = new GeometryFactory().createLinearRing(coords1)
    val holes1: Array[LinearRing] = null

    val polygon1: Polygon = new GeometryFactory().createPolygon(ring1, holes1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(2, 0),
      new Coordinate(0, 2),
      new Coordinate(2, 4),
      new Coordinate(5, 2),
      new Coordinate(2, 0))

    val ring2: LinearRing = new GeometryFactory().createLinearRing(coords2)
    val holes2: Array[LinearRing] = null
    val polygon2: Polygon = new GeometryFactory().createPolygon(ring2, holes2)

    val points: List[Point] = List(
      geometryFactory.createPoint(new Coordinate(4, 2)),
      geometryFactory.createPoint(new Coordinate(3, 2)),
      geometryFactory.createPoint(new Coordinate(1, 2)))
    val pointsIntersected: List[Point] =
      List(geometryFactory.createPoint(new Coordinate(4, 2)), geometryFactory.createPoint(new Coordinate(3, 2)))

    val wrapper = new GeometriesOperations
    val result = wrapper.getIntersectionPoints(polygon1, polygon2, points)
    result.size shouldBe 2
    result.sameElements(pointsIntersected) shouldBe true
  }

  "The intersection" should "be return an empty list" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val ring1: LinearRing = new GeometryFactory().createLinearRing(coords1)
    val holes1: Array[LinearRing] = null

    val polygon1: Polygon = new GeometryFactory().createPolygon(ring1, holes1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(2, 0),
      new Coordinate(0, 2),
      new Coordinate(2, 4),
      new Coordinate(5, 2),
      new Coordinate(2, 0))

    val ring2: LinearRing = new GeometryFactory().createLinearRing(coords2)
    val holes2: Array[LinearRing] = null
    val polygon2: Polygon = new GeometryFactory().createPolygon(ring2, holes2)

    val points: List[Point] = List(
      geometryFactory.createPoint(new Coordinate(8, 1)),
      geometryFactory.createPoint(new Coordinate(8, 2)),
      geometryFactory.createPoint(new Coordinate(1, 2)))

    val wrapper = new GeometriesOperations
    val result = wrapper.getIntersectionPoints(polygon1, polygon2, points)
    result.size shouldBe 0
  }
}
