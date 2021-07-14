package it.agilelab.gis.domain.model

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory, LinearRing, Polygon }
import it.agilelab.gis.domain.models.GeometriesWrapper
import org.scalatest.{ FlatSpec, Matchers }

class GeometriesWrapperSuite extends FlatSpec with Matchers {

  "GeometryWrapper" should "compute the distance between two polygons" in {

    val geometryFactory: GeometryFactory = new GeometryFactory()

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val ring1: LinearRing = geometryFactory.createLinearRing(coords1)
    val holes1: Array[LinearRing] = null
    val polygon1: Polygon = geometryFactory.createPolygon(ring1, holes1)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(8, 1),
      new Coordinate(7, 2),
      new Coordinate(8, 3),
      new Coordinate(9, 2),
      new Coordinate(8, 1))

    val ring2: LinearRing = geometryFactory.createLinearRing(coords2)
    val holes2: Array[LinearRing] = null
    val polygon2: Polygon = geometryFactory.createPolygon(ring2, holes2)

    val wrapper = new GeometriesWrapper(polygon1, polygon2)
    val result = wrapper.computeDistance

    result shouldBe 1
  }

  "GeometryWrapper" should "compute the distance between a polygon and a point" in {

    val geometryFactory: GeometryFactory = new GeometryFactory()

    val coords1: Array[Coordinate] = Array[Coordinate](
      new Coordinate(4, 0),
      new Coordinate(2, 2),
      new Coordinate(4, 4),
      new Coordinate(6, 2),
      new Coordinate(4, 0))

    val ring1: LinearRing = geometryFactory.createLinearRing(coords1)
    val holes1: Array[LinearRing] = null
    val polygon: Polygon = geometryFactory.createPolygon(ring1, holes1)

    val coords2 = new Coordinate(1, 2)
    val point = geometryFactory.createPoint(coords2)

    val wrapper = new GeometriesWrapper(polygon, point)
    val result = wrapper.computeDistance
    result shouldBe 1
  }
}
