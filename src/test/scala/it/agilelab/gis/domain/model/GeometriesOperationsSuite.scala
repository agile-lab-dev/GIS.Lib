package it.agilelab.gis.domain.model

import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory, LineString, LinearRing, Point, Polygon }
import com.vividsolutions.jts.util.GeometricShapeFactory
import it.agilelab.gis.domain.models.GeometriesOperations
import org.scalatest.{ FlatSpec, Matchers }

class GeometriesOperationsSuite extends FlatSpec with Matchers {

  private val geometryFactory: GeometryFactory = new GeometryFactory()

  /** Polygon vs Polygon */
  "Polygon 1" should "contains Polygon 2" in {

    // makes a Polygon in the shape of a square
    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon1: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 1),
      new Coordinate(3, 2),
      new Coordinate(4, 3),
      new Coordinate(5, 2),
      new Coordinate(4, 1))
    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val result = GeometriesOperations.contains(polygon1, polygon2)
    result shouldBe true
  }

  "Polygon 3" should "intersecs Polygon 1 but it should be not contained in Polygon 1" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon1: Polygon = geometryFactory.createPolygon(coords1)

    val coords3: Array[Coordinate] = Array[Coordinate](
      new Coordinate(2, 0),
      new Coordinate(0, 2),
      new Coordinate(2, 4),
      new Coordinate(4, 2),
      new Coordinate(2, 0))
    val polygon3: Polygon = geometryFactory.createPolygon(coords3)

    val result = GeometriesOperations.contains(polygon1, polygon3)
    result shouldBe false
  }

  "Polygon 1" should "not contains Polygon 2" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon1: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(8, 0),
      new Coordinate(7, 1),
      new Coordinate(8, 2),
      new Coordinate(9, 1),
      new Coordinate(8, 0))
    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val result = GeometriesOperations.contains(polygon1, polygon2)
    result shouldBe false
  }

  /** Polygon vs Point */
  "Polygon" should "contains Point" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val point: Point = geometryFactory.createPoint(new Coordinate(4, 1))

    val result = GeometriesOperations.contains(polygon, point)
    result shouldBe true
  }

  "Polygon" should "not contains Point" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val point: Point = geometryFactory.createPoint(new Coordinate(6, 1))

    val result = GeometriesOperations.contains(polygon, point)
    result shouldBe false
  }

  /** Circle vs Point */
  "Circle" should "contains Point" in {

    val geometryShapeFactory: GeometricShapeFactory = new GeometricShapeFactory()
    geometryShapeFactory.setNumPoints(20)
    geometryShapeFactory.setCentre(new Coordinate(4, 2))
    geometryShapeFactory.setSize(4)
    val circle: Geometry = geometryShapeFactory.createCircle()

    val point: Point = geometryFactory.createPoint(new Coordinate(5, 2))

    val result = GeometriesOperations.contains(circle, point)
    result shouldBe true
  }

  "Circle" should "not contains Point" in {

    val geometryShapeFactory: GeometricShapeFactory = new GeometricShapeFactory()
    geometryShapeFactory.setNumPoints(20)
    geometryShapeFactory.setCentre(new Coordinate(4, 2))
    geometryShapeFactory.setSize(4)
    val circle: Geometry = geometryShapeFactory.createCircle()

    val point: Point = geometryFactory.createPoint(new Coordinate(1, 1))

    val result = GeometriesOperations.contains(circle, point)
    result shouldBe false
  }

  /** Line vs Point */
  "LineString" should "contains Point" in {

    val coords: Array[Coordinate] =
      Array[Coordinate](new Coordinate(0, 2), new Coordinate(2, 0), new Coordinate(2, 1), new Coordinate(2, 3))
    val linestring: LineString = geometryFactory.createLineString(coords)

    val point: Point = geometryFactory.createPoint(new Coordinate(2, 2))

    val result = GeometriesOperations.contains(linestring, point)
    result shouldBe true
  }

  "LineString" should "not contains Point" in {

    val coords: Array[Coordinate] =
      Array[Coordinate](new Coordinate(0, 2), new Coordinate(2, 0), new Coordinate(2, 1), new Coordinate(2, 3))
    val linestring: LineString = geometryFactory.createLineString(coords)

    val point: Point = geometryFactory.createPoint(new Coordinate(1, 2))

    val result = GeometriesOperations.contains(linestring, point)
    result shouldBe false
  }
}
