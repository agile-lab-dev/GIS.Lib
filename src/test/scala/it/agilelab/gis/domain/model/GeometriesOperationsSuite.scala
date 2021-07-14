package it.agilelab.gis.domain.model

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory, LineString, LinearRing, Point, Polygon }
import it.agilelab.gis.domain.models.GeometriesOperations
import org.scalatest.{ FlatSpec, Matchers }

class GeometriesOperationsSuite extends FlatSpec with Matchers {

  private val geometryFactory: GeometryFactory = new GeometryFactory()

  /** Polygon vs Polygon */
  "The distance between polygons" should "be greater than 0" in {

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val polygon1: Polygon = geometryFactory.createPolygon(coords1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(8, 1),
      new Coordinate(7, 2),
      new Coordinate(8, 3),
      new Coordinate(9, 2),
      new Coordinate(8, 1))

    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val result = GeometriesOperations.computeDistance(polygon1, polygon2)
    result shouldBe 1
  }

  "The distance between polygons" should "be 0 - Polygon1 contains Polygon2" in {

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

    val result = GeometriesOperations.computeDistance(polygon1, polygon2)
    result shouldBe 0
  }

  /** Polygon vs Point */
  "The distance between Polygon and Point" should "be greater than 0" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val point = geometryFactory.createPoint(new Coordinate(1, 2))

    val result = GeometriesOperations.computeDistance(polygon, point)
    result shouldBe 1
  }

  "The distance between Polygon and Point" should "be 0 - Polygon contains Point" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val point = geometryFactory.createPoint(new Coordinate(3, 2))

    val result = GeometriesOperations.computeDistance(polygon, point)
    result shouldBe 0
  }

  /** Circle vs Point */
  "The distance between Circle and Point" should "be greater than 0" in {

    val geometryShapeFactory: GeometricShapeFactory = new GeometricShapeFactory()
    geometryShapeFactory.setNumPoints(20)
    geometryShapeFactory.setCentre(new Coordinate(4, 2))
    geometryShapeFactory.setSize(4)
    val circle: Geometry = geometryShapeFactory.createCircle()

    val point: Point = geometryFactory.createPoint(new Coordinate(7, 2))

    val result = GeometriesOperations.computeDistance(circle, point)
    result shouldBe 1
  }

  /** Line vs Line */
  "The distance between lines" should "be greater than 0" in {

    val linestring1: LineString = geometryFactory.createLineString(
      new Coordinate(0, 2),
      new Coordinate(2, 0),
      new Coordinate(2, 1),
      new Coordinate(2, 3))
    val linestring2: LineString =
      geometryFactory.createLineString(new Coordinate(3, 4), new Coordinate(4, 5), new Coordinate(6, 1))

    val result = GeometriesOperations.computeDistance(linestring1, linestring2)
    result shouldBe 1
  }

  "The distance between lines" should "be 0 - the lines are intersected" in {

    val linestring1: LineString = geometryFactory.createLineString(
      new Coordinate(0, 2),
      new Coordinate(2, 0),
      new Coordinate(2, 1),
      new Coordinate(2, 3))
    val linestring2: LineString =
      geometryFactory.createLineString(new Coordinate(1, 3), new Coordinate(2, 3), new Coordinate(3, 4))

    val result = GeometriesOperations.computeDistance(linestring1, linestring2)
    result shouldBe 0
  }

}
