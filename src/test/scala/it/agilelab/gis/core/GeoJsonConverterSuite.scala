package it.agilelab.gis.core

import com.vividsolutions.jts.geom.{
  Coordinate,
  Geometry,
  GeometryFactory,
  LineString,
  MultiLineString,
  MultiPoint,
  MultiPolygon,
  Point,
  Polygon
}
import org.scalatest.{ FlatSpec, Matchers }
import it.agilelab.gis.core.utils.JsonImplicit._
import spray.json._
import DefaultJsonProtocol._
import it.agilelab.gis.core.utils.{
  BooleanResult,
  DoubleResult,
  FeatureCollection,
  GeometryFeature,
  GeometryTypeOperations,
  TypeOperation
}

class GeoJsonConverterSuite extends FlatSpec with Matchers {

  private val geometryFactory: GeometryFactory = new GeometryFactory()

  "GeoJsonConverter" should "convert a geometry point into a json point" in {

    val point: Point = geometryFactory.createPoint(new Coordinate(46.0677293, 11.1215698))

    val jsonpoint = point.asInstanceOf[Geometry].toJson
    val expected = """{"coordinates":[46.0677293,11.1215698],"type":"Point"}"""

    jsonpoint.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert a json point into a geometry point" in {

    val point: Point = geometryFactory.createPoint(new Coordinate(46.0677293, 11.1215698))

    val jsonpoint = point.asInstanceOf[Geometry].toJson
    val geopoint = jsonpoint.convertTo[Geometry]
    val expected = """POINT (46.0677293 11.1215698)"""

    geopoint.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert a geometry linestring into a json linestring" in {

    val lineString: LineString = geometryFactory.createLineString(
      Array(new Coordinate(11.1212678, 46.0686443), new Coordinate(11.1212316, 46.0688409)))
    val jsonlinestring = lineString.asInstanceOf[Geometry].toJson
    val expected =
      """{"coordinates":[[11.1212678,46.0686443],[11.1212316,46.0688409]],"type":"LineString"}""".stripMargin

    jsonlinestring.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert a json linestring into a geometry linestring" in {

    val lineString: LineString = geometryFactory.createLineString(
      Array(new Coordinate(11.1212678, 46.0686443), new Coordinate(11.1212316, 46.0688409)))
    val jsonlinestring = lineString.asInstanceOf[Geometry].toJson
    val geolinestring = jsonlinestring.convertTo[Geometry]
    val expected = """LINESTRING (11.1212678 46.0686443, 11.1212316 46.0688409)"""

    geolinestring.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert a geometry polygon into a json polygon" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(30, 10),
      new Coordinate(40, 40),
      new Coordinate(20, 40),
      new Coordinate(10, 20),
      new Coordinate(30, 10))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val jsonpolygon = polygon.asInstanceOf[Geometry].toJson

    val expected =
      """{"coordinates":[[[30.0,10.0],[40.0,40.0],[20.0,40.0],[10.0,20.0],[30.0,10.0]]],"type":"Polygon"}""".stripMargin

    jsonpolygon.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert a json polygon into a geometry polygon" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(30, 10),
      new Coordinate(40, 40),
      new Coordinate(20, 40),
      new Coordinate(10, 20),
      new Coordinate(30, 10))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val jsonpolygon = polygon.asInstanceOf[Geometry].toJson

    val geopolygon = jsonpolygon.convertTo[Geometry]
    val expected = """POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))"""

    geopolygon.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert a geometry multipoint into a json multipoint" in {

    val point1: Point = geometryFactory.createPoint(new Coordinate(10, 40))
    val point2: Point = geometryFactory.createPoint(new Coordinate(40, 30))
    val point3: Point = geometryFactory.createPoint(new Coordinate(20, 20))
    val point4: Point = geometryFactory.createPoint(new Coordinate(30, 10))

    val multipoint: MultiPoint = geometryFactory.createMultiPoint(Array(point1, point2, point3, point4))

    val jsonmultipoint = multipoint.asInstanceOf[Geometry].toJson

    val expected =
      """{"coordinates":[[10.0,40.0],[40.0,30.0],[20.0,20.0],[30.0,10.0]],"type":"MultiPoint"}""".stripMargin

    jsonmultipoint.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert a json multipoint into a geometry multipoint" in {

    val point1: Point = geometryFactory.createPoint(new Coordinate(10, 40))
    val point2: Point = geometryFactory.createPoint(new Coordinate(40, 30))
    val point3: Point = geometryFactory.createPoint(new Coordinate(20, 20))
    val point4: Point = geometryFactory.createPoint(new Coordinate(30, 10))

    val multipoint: MultiPoint = geometryFactory.createMultiPoint(Array(point1, point2, point3, point4))

    val jsonmultipoint = multipoint.asInstanceOf[Geometry].toJson
    val geomultipoint = jsonmultipoint.convertTo[Geometry]
    val expected = """MULTIPOINT ((10 40), (40 30), (20 20), (30 10))""".stripMargin

    geomultipoint.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert a geometry multilinestring into a json multilinestring" in {

    val lineString1: LineString =
      geometryFactory.createLineString(Array(new Coordinate(10, 10), new Coordinate(20, 20), new Coordinate(10, 40)))

    val lineString2: LineString = geometryFactory.createLineString(
      Array(new Coordinate(40, 40), new Coordinate(30, 30), new Coordinate(40, 20), new Coordinate(30, 10)))

    val multilinestring: MultiLineString = geometryFactory.createMultiLineString(Array(lineString1, lineString2))

    val jsonmultilinestring = multilinestring.asInstanceOf[Geometry].toJson

    val expected =
      """{"coordinates":[[[10.0,10.0],[20.0,20.0],[10.0,40.0]],[[40.0,40.0],[30.0,30.0],[40.0,20.0],[30.0,10.0]]],"type":"MultiLineString"}""".stripMargin

    jsonmultilinestring.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert a json multilinestring into a geometry multilinestring" in {

    val lineString1: LineString =
      geometryFactory.createLineString(Array(new Coordinate(10, 10), new Coordinate(20, 20), new Coordinate(10, 40)))

    val lineString2: LineString = geometryFactory.createLineString(
      Array(new Coordinate(40, 40), new Coordinate(30, 30), new Coordinate(40, 20), new Coordinate(30, 10)))

    val multilinestring: MultiLineString = geometryFactory.createMultiLineString(Array(lineString1, lineString2))

    val jsonmultilinestring = multilinestring.asInstanceOf[Geometry].toJson
    val geomultilinestring = jsonmultilinestring.convertTo[Geometry]

    val expected = """MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10))""".stripMargin

    geomultilinestring.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert a geometry multipolygon into a json multipolygon" in {

    val coords: Array[Coordinate] =
      Array[Coordinate](new Coordinate(30, 20), new Coordinate(45, 40), new Coordinate(10, 40), new Coordinate(30, 20))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(15, 5),
      new Coordinate(40, 10),
      new Coordinate(10, 20),
      new Coordinate(5, 10),
      new Coordinate(15, 5))
    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val multipolygon: MultiPolygon = geometryFactory.createMultiPolygon(Array(polygon, polygon2))
    val jsonmultipolygon = multipolygon.asInstanceOf[Geometry].toJson

    val expected =
      """{"coordinates":[[[[30.0,20.0],[45.0,40.0],[10.0,40.0],[30.0,20.0]]],[[[15.0,5.0],[40.0,10.0],[10.0,20.0],[5.0,10.0],[15.0,5.0]]]],"type":"MultiPolygon"}""".stripMargin

    jsonmultipolygon.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert a json multipolygon into a geometry multipolygon" in {

    val coords: Array[Coordinate] =
      Array[Coordinate](new Coordinate(30, 20), new Coordinate(45, 40), new Coordinate(10, 40), new Coordinate(30, 20))
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val coords2: Array[Coordinate] = Array[Coordinate](
      new Coordinate(15, 5),
      new Coordinate(40, 10),
      new Coordinate(10, 20),
      new Coordinate(5, 10),
      new Coordinate(15, 5))
    val polygon2: Polygon = geometryFactory.createPolygon(coords2)

    val multipolygon: MultiPolygon = geometryFactory.createMultiPolygon(Array(polygon, polygon2))
    val jsonmultipolygon = multipolygon.asInstanceOf[Geometry].toJson

    val geomultipolygon = jsonmultipolygon.convertTo[Geometry]

    val expected = """MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), ((15 5, 40 10, 10 20, 5 10, 15 5)))""".stripMargin

    geomultipolygon.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert the coords of the Italy into a json polygon" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(8.89892578125, 44.43377984606822),
      new Coordinate(10.01953125, 44.18220395771566),
      new Coordinate(10.9423828125, 42.52069952914966),
      new Coordinate(15.073242187499998, 40.34654412118006),
      new Coordinate(16.1279296875, 39.095962936305476),
      new Coordinate(15.205078125, 38.30718056188316),
      new Coordinate(12.963867187499998, 38.13455657705411),
      new Coordinate(12.6123046875, 37.61423141542417),
      new Coordinate(15.161132812500002, 36.66841891894786),
      new Coordinate(15.205078125, 38.06539235133249),
      new Coordinate(15.886230468750002, 37.97884504049713),
      new Coordinate(17.2265625, 39.06184913429154),
      new Coordinate(16.435546875, 39.52099229357195),
      new Coordinate(16.63330078125, 40.3130432088809),
      new Coordinate(18.08349609375, 39.791654835253425),
      new Coordinate(18.369140624999996, 40.36328834091583),
      new Coordinate(15.09521484375, 41.77131167976407),
      new Coordinate(13.447265624999998, 43.50075243569041),
      new Coordinate(11.997070312499998, 44.512176171071054),
      new Coordinate(12.41455078125, 45.460130637921004),
      new Coordinate(12.919921874999998, 46.37725420510028),
      new Coordinate(11.162109375, 46.73986059969267),
      new Coordinate(9.7998046875, 45.99696161820381),
      new Coordinate(8.2177734375, 46.01222384063236),
      new Coordinate(7.27294921875, 45.07352060670971),
      new Coordinate(7.448730468749999, 44.276671273775186),
      new Coordinate(8.89892578125, 44.43377984606822)
    )
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val jsonpolygon = polygon.asInstanceOf[Geometry].toJson

    val expected =
      """{"coordinates":[[[8.89892578125,44.43377984606822],[10.01953125,44.18220395771566],[10.9423828125,42.52069952914966],[15.073242187499998,40.34654412118006],[16.1279296875,39.095962936305476],[15.205078125,38.30718056188316],[12.963867187499998,38.13455657705411],[12.6123046875,37.61423141542417],[15.161132812500002,36.66841891894786],[15.205078125,38.06539235133249],[15.886230468750002,37.97884504049713],[17.2265625,39.06184913429154],[16.435546875,39.52099229357195],[16.63330078125,40.3130432088809],[18.08349609375,39.791654835253425],[18.369140624999996,40.36328834091583],[15.09521484375,41.77131167976407],[13.447265624999998,43.50075243569041],[11.997070312499998,44.512176171071054],[12.41455078125,45.460130637921004],[12.919921874999998,46.37725420510028],[11.162109375,46.73986059969267],[9.7998046875,45.99696161820381],[8.2177734375,46.01222384063236],[7.27294921875,45.07352060670971],[7.448730468749999,44.276671273775186],[8.89892578125,44.43377984606822]]],"type":"Polygon"}""".stripMargin

    jsonpolygon.toString() shouldBe expected
  }

  "GeoJsonConverter" should "convert the coords of the Italy into a geometry polygon" in {

    val coords: Array[Coordinate] = Array[Coordinate](
      new Coordinate(8.89892578125, 44.43377984606822),
      new Coordinate(10.01953125, 44.18220395771566),
      new Coordinate(10.9423828125, 42.52069952914966),
      new Coordinate(15.073242187499998, 40.34654412118006),
      new Coordinate(16.1279296875, 39.095962936305476),
      new Coordinate(15.205078125, 38.30718056188316),
      new Coordinate(12.963867187499998, 38.13455657705411),
      new Coordinate(12.6123046875, 37.61423141542417),
      new Coordinate(15.161132812500002, 36.66841891894786),
      new Coordinate(15.205078125, 38.06539235133249),
      new Coordinate(15.886230468750002, 37.97884504049713),
      new Coordinate(17.2265625, 39.06184913429154),
      new Coordinate(16.435546875, 39.52099229357195),
      new Coordinate(16.63330078125, 40.3130432088809),
      new Coordinate(18.08349609375, 39.791654835253425),
      new Coordinate(18.369140624999996, 40.36328834091583),
      new Coordinate(15.09521484375, 41.77131167976407),
      new Coordinate(13.447265624999998, 43.50075243569041),
      new Coordinate(11.997070312499998, 44.512176171071054),
      new Coordinate(12.41455078125, 45.460130637921004),
      new Coordinate(12.919921874999998, 46.37725420510028),
      new Coordinate(11.162109375, 46.73986059969267),
      new Coordinate(9.7998046875, 45.99696161820381),
      new Coordinate(8.2177734375, 46.01222384063236),
      new Coordinate(7.27294921875, 45.07352060670971),
      new Coordinate(7.448730468749999, 44.276671273775186),
      new Coordinate(8.89892578125, 44.43377984606822)
    )
    val polygon: Polygon = geometryFactory.createPolygon(coords)

    val jsonpolygon = polygon.asInstanceOf[Geometry].toJson

    val geopolygon = jsonpolygon.convertTo[Geometry]
    val expected =
      """POLYGON ((8.89892578125 44.43377984606822, 10.01953125 44.18220395771566, 10.9423828125 42.52069952914966, 15.073242187499998 40.34654412118006, 16.1279296875 39.095962936305476, 15.205078125 38.30718056188316, 12.963867187499998 38.13455657705411, 12.6123046875 37.61423141542417, 15.161132812500002 36.66841891894786, 15.205078125 38.06539235133249, 15.886230468750002 37.97884504049713, 17.2265625 39.06184913429154, 16.435546875 39.52099229357195, 16.63330078125 40.3130432088809, 18.08349609375 39.791654835253425, 18.369140624999996 40.36328834091583, 15.09521484375 41.77131167976407, 13.447265624999998 43.50075243569041, 11.997070312499998 44.512176171071054, 12.41455078125 45.460130637921004, 12.919921874999998 46.37725420510028, 11.162109375 46.73986059969267, 9.7998046875 45.99696161820381, 8.2177734375 46.01222384063236, 7.27294921875 45.07352060670971, 7.448730468749999 44.276671273775186, 8.89892578125 44.43377984606822))""".stripMargin

    geopolygon.toString shouldBe expected
  }

  "GeoJsonConverter" should "convert the boolean operation result to json" in {
    val operationResult = BooleanResult(true).toJson.toString()
    val expected = """{"operationResult":true}"""
    operationResult shouldBe expected
  }

  "GeoJsonConverter" should "read the json boolean operation result" in {
    val operationResult = BooleanResult(true).toJson
    val readResult = operationResult.convertTo[BooleanResult].operationResult
    readResult shouldBe true
  }

  "GeoJsonConverter" should "convert the double operation result to json" in {
    val operationResult = DoubleResult(2.9273545).toJson.toString()
    val expected = """{"operationResult":2.9273545}"""
    operationResult shouldBe expected
  }

  "GeoJsonConverter" should "read the json double operation result" in {
    val operationResult = DoubleResult(2.9273545).toJson
    val readResult = operationResult.convertTo[DoubleResult].operationResult
    readResult shouldBe 2.9273545
  }

  "GeoJsonConverter" should "convert the operation type ti json" in {
    val typeOperation = TypeOperation(GeometryTypeOperations.Distance).toJson.toString()
    val expected = """{"typeOperation":"distance"}"""
    typeOperation shouldBe expected
  }

  "GeoJsonConverter" should "read the json type operation" in {
    val typeOperation = TypeOperation(GeometryTypeOperations.Contains).toJson
    val readResult = typeOperation.convertTo[TypeOperation].typeOperation
    readResult shouldBe GeometryTypeOperations.Contains
  }

  "GeoJsonConverter" should "convert the geometry feature to json" in {
    val geometryFeature: GeometryFeature =
      GeometryFeature("Feature", Map("typeOperation" -> "distance"), geometryFactory.createPoint(new Coordinate(2, 2)))
    val expected =
      """{"geometry":{"coordinates":[2.0,2.0],"type":"Point"},"properties":{"typeOperation":"distance"},"type":"Feature"}"""
    geometryFeature.toJson.toString() shouldBe expected
  }

  "GeoJsonConverter" should "read the json geometry feature" in {
    val jsonGeometryFeature = GeometryFeature(
      "Feature",
      Map("typeOperation" -> "distance"),
      geometryFactory.createPoint(new Coordinate(2, 2))).toJson
    val geometryFeature = jsonGeometryFeature.convertTo[GeometryFeature]
    geometryFeature.`type` shouldBe "Feature"
    geometryFeature.geometry.toString shouldBe "POINT (2 2)"
    geometryFeature.properties shouldBe Map("typeOperation" -> "distance")
  }

  "GeoJsonConverter" should "convert the geometry collection to json" in {
    val geometryFeature1: GeometryFeature =
      GeometryFeature("Feature", Map("typeOperation" -> "distance"), geometryFactory.createPoint(new Coordinate(2, 2)))
    val geometryFeature2: GeometryFeature =
      GeometryFeature("Feature", Map("typeOperation" -> "distance"), geometryFactory.createPoint(new Coordinate(1, 2)))

    val featureCollection: FeatureCollection =
      FeatureCollection("FeatureCollection", Array(geometryFeature1, geometryFeature2))

    val expected =
      """{"features":[{"geometry":{"coordinates":[2.0,2.0],"type":"Point"},"properties":{"typeOperation":"distance"},"type":"Feature"},{"geometry":{"coordinates":[1.0,2.0],"type":"Point"},"properties":{"typeOperation":"distance"},"type":"Feature"}],"type":"FeatureCollection"}"""
    featureCollection.toJson.toString() shouldBe expected
  }

  "GeoJsonConverter" should "read the json geometry collection" in {
    val geometryFeature1: GeometryFeature =
      GeometryFeature("Feature", Map("typeOperation" -> "distance"), geometryFactory.createPoint(new Coordinate(2, 2)))
    val geometryFeature2: GeometryFeature =
      GeometryFeature("Feature", Map("typeOperation" -> "distance"), geometryFactory.createPoint(new Coordinate(1, 2)))

    val jsonFeatureCollection: JsValue =
      FeatureCollection("FeatureCollection", Array(geometryFeature1, geometryFeature2)).toJson

    val featureCollection = jsonFeatureCollection.convertTo[FeatureCollection]
    featureCollection.`type` shouldBe "FeatureCollection"
    featureCollection.features shouldBe Array(geometryFeature1, geometryFeature2)
  }
}
