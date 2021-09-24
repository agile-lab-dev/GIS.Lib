package it.agilelab.gis.domain.manager

import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory, LineString, MultiPoint, Point, Polygon }
import com.vividsolutions.jts.util.GeometricShapeFactory
import it.agilelab.gis.domain.managers.GeometriesOperations
import org.scalatest.{ FlatSpec, Matchers }

class GeometriesOperationsSuite extends FlatSpec with Matchers {

  private val geometryFactory: GeometryFactory = new GeometryFactory()

  /* *******************************
   *   Distance functionality  *
   ********************************* */
  /** Point vs Point */
  "The distance between points" should "be greater than 0" in {

    val point1: Point = geometryFactory.createPoint(new Coordinate(3, 2))
    val point2: Point = geometryFactory.createPoint(new Coordinate(3, 3))

    val result = GeometriesOperations.computePointsDistance(point1, point2)
    (result / 1000).toInt shouldBe 111
  }

  "The distance between points" should "be 0 - the points are overlapped" in {

    val point1: Point = geometryFactory.createPoint(new Coordinate(3, 2))
    val point2: Point = geometryFactory.createPoint(new Coordinate(3, 2))

    val result = GeometriesOperations.computePointsDistance(point1, point2)
    result shouldBe 0
  }

  /* *******************************
   *   Intersection functionality  *
   ********************************* */

  /** Polygon vs Polygon */
  "The intersection" should "be return the list of points inside the intersection of two polygons" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val polygon1: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(2, 0),
      new Coordinate(0, 2),
      new Coordinate(2, 4),
      new Coordinate(5, 2),
      new Coordinate(2, 0))
    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(4, 2)),
      geometryFactory.createPoint(new Coordinate(3, 2)),
      geometryFactory.createPoint(new Coordinate(1, 2)))
    val pointsIntersected: Array[Point] =
      Array(geometryFactory.createPoint(new Coordinate(4, 2)), geometryFactory.createPoint(new Coordinate(3, 2)))
    val multipoint = geometryFactory.createMultiPoint(pointsIntersected)

    val result: MultiPoint = GeometriesOperations.getIntersectionPoints(polygon1, polygon2, points)
    result.getNumGeometries shouldBe 2
    result.equals(multipoint) shouldBe true
  }

  "The intersection" should "be return an empty list - no points within the insersection between two polygons" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon1: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(2, 0),
      new Coordinate(0, 2),
      new Coordinate(2, 4),
      new Coordinate(5, 2),
      new Coordinate(2, 0))
    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(8, 1)),
      geometryFactory.createPoint(new Coordinate(8, 2)),
      geometryFactory.createPoint(new Coordinate(1, 2)))

    val result = GeometriesOperations.getIntersectionPoints(polygon1, polygon2, points)
    result.getNumGeometries shouldBe 0
  }

  /** Polygon vs LineString */
  "The intersection" should "return the list of points within the insersection between a polygon and a linestring" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] =
      Array[Coordinate](new Coordinate(7, 1), new Coordinate(4, 1), new Coordinate(4, 2), new Coordinate(8, 3))
    val linestring: LineString =
      geometryFactory.createLineString(coords2)

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(4, 1)),
      geometryFactory.createPoint(new Coordinate(4, 3)),
      geometryFactory.createPoint(new Coordinate(4, 1.5)),
      geometryFactory.createPoint(new Coordinate(7, 2)),
      geometryFactory.createPoint(new Coordinate(5, 2))
    )

    val pointsIntersected: Array[Point] =
      Array(geometryFactory.createPoint(new Coordinate(4, 1)), geometryFactory.createPoint(new Coordinate(4, 1.5)))

    val multipoint: MultiPoint = geometryFactory.createMultiPoint(pointsIntersected)

    val result = GeometriesOperations.getIntersectionPoints(polygon, linestring, points)
    result.getNumGeometries shouldBe 2
    result.equals(multipoint) shouldBe true
  }

  "The intersection" should "return an empty list - polygon and a linestring do not intersect" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))
    val polygon: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] =
      Array[Coordinate](new Coordinate(8, 1), new Coordinate(8, 2), new Coordinate(8, 3))
    val linestring: LineString =
      geometryFactory.createLineString(coords2)

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(4, 1)),
      geometryFactory.createPoint(new Coordinate(4, 3)),
      geometryFactory.createPoint(new Coordinate(4, 1.5)),
      geometryFactory.createPoint(new Coordinate(7, 2)),
      geometryFactory.createPoint(new Coordinate(5, 2))
    )

    val result = GeometriesOperations.getIntersectionPoints(polygon, linestring, points)
    result.getNumGeometries shouldBe 0
  }

  /** Circle vs Polygon */
  "The intersection" should "return the list of points within the insersection between a circle and a polygon" in {

    val geometryShapeFactory: GeometricShapeFactory = new GeometricShapeFactory()
    geometryShapeFactory.setNumPoints(30)
    geometryShapeFactory.setCentre(new Coordinate(4, 2))
    geometryShapeFactory.setSize(6)
    val circle: Geometry = geometryShapeFactory.createCircle()

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 1),
      new Coordinate(3, 2),
      new Coordinate(5, 4),
      new Coordinate(6, 3),
      new Coordinate(4, 1))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(4, 2)),
      geometryFactory.createPoint(new Coordinate(3, 1)),
      geometryFactory.createPoint(new Coordinate(5, 3.5)),
      geometryFactory.createPoint(new Coordinate(6, 1)),
      geometryFactory.createPoint(new Coordinate(8, 2))
    )

    val pointsIntersected: Array[Point] =
      Array(geometryFactory.createPoint(new Coordinate(4, 2)), geometryFactory.createPoint(new Coordinate(5, 3.5)))

    val multipoint: MultiPoint = geometryFactory.createMultiPoint(pointsIntersected)

    val result = GeometriesOperations.getIntersectionPoints(circle, polygon, points)
    result.getNumGeometries shouldBe 2
    result.equals(multipoint) shouldBe true
  }

  "The intersection" should "return an empty list - no points within the insersection between a circle and a polygon" in {

    val geometryShapeFactory: GeometricShapeFactory = new GeometricShapeFactory()
    geometryShapeFactory.setNumPoints(30)
    geometryShapeFactory.setCentre(new Coordinate(4, 2))
    geometryShapeFactory.setSize(6)
    val circle: Geometry = geometryShapeFactory.createCircle()

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 1),
      new Coordinate(3, 2),
      new Coordinate(5, 4),
      new Coordinate(6, 3),
      new Coordinate(4, 1))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(3, 1)),
      geometryFactory.createPoint(new Coordinate(6, 1)),
      geometryFactory.createPoint(new Coordinate(8, 2))
    )

    val result = GeometriesOperations.getIntersectionPoints(circle, polygon, points)
    result.getNumGeometries shouldBe 0
  }

  /** LineString vs Point */
  "The intersection" should "return the point within the insersection between a linestring and a point" in {

    val coords: Array[Coordinate] =
      Array[Coordinate](new Coordinate(7, 1), new Coordinate(4, 1), new Coordinate(4, 2), new Coordinate(8, 3))
    val linestring: LineString =
      geometryFactory.createLineString(coords)

    val point: Point = geometryFactory.createPoint(new Coordinate(4, 1.5))

    val points: Array[Point] = Array(
      geometryFactory.createPoint(new Coordinate(3, 1)),
      geometryFactory.createPoint(new Coordinate(6, 1)),
      geometryFactory.createPoint(new Coordinate(4, 1.5)),
      geometryFactory.createPoint(new Coordinate(8, 2))
    )

    val pointsIntersected: Array[Point] =
      Array(geometryFactory.createPoint(new Coordinate(4, 1.5)))

    val multipoint: MultiPoint = geometryFactory.createMultiPoint(pointsIntersected)

    val result = GeometriesOperations.getIntersectionPoints(linestring, point, points)
    result.getNumGeometries shouldBe 1
    result.equals(multipoint) shouldBe true
  }

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
