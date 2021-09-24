package it.agilelab.gis.core.utils

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
import spray.json._
import DefaultJsonProtocol._

case class BooleanResult(operationResult: Boolean)
case class DoubleResult(operationResult: Double)
case class TypeOperation(typeOperation: GeometryTypeOperations)
case class GeometryFeature(`type`: String, properties: Map[String, String], geometry: Geometry)
case class FeatureCollection(`type`: String, features: Array[GeometryFeature])

object JsonImplicit extends DefaultJsonProtocol {

  implicit val booleanFormat: RootJsonFormat[BooleanResult] = jsonFormat1(BooleanResult.apply)
  implicit val doubleFormat: RootJsonFormat[DoubleResult] = jsonFormat1(DoubleResult.apply)

  implicit object GeometryTypeOperationsFormat extends RootJsonFormat[GeometryTypeOperations] {
    override def write(obj: GeometryTypeOperations): JsValue = JsString(obj.name)

    override def read(json: JsValue): GeometryTypeOperations =
      json match {
        case JsString("distance")     => GeometryTypeOperations.Distance
        case JsString("intersection") => GeometryTypeOperations.Intersection
        case JsString("contains")     => GeometryTypeOperations.Contains
        case _                        => deserializationError(s"'$json' is not a valid GeometryTypeOperations")
      }
  }

  implicit val typeOperationsFormat: RootJsonFormat[TypeOperation] = jsonFormat1(TypeOperation.apply)

  implicit object CoordinateFormat extends RootJsonFormat[Coordinate] {
    override def write(obj: Coordinate): JsValue = JsArray(
      JsNumber(obj.x),
      JsNumber(obj.y)
    )

    override def read(json: JsValue): Coordinate = json match {
      case JsArray(is) if is.length == 2 || is.length == 3 =>
        new Coordinate(is(0).convertTo[Double], is(1).convertTo[Double])
      case _ => deserializationError(s"'$json' is not a valid Coordinate")
    }
  }

  implicit object GeometryFormat extends RootJsonFormat[Geometry] {
    override def write(obj: Geometry): JsValue = {
      val c = obj.getClass
      if (c == classOf[Point]) {
        JsObject(
          "type"        -> JsString("Point"),
          "coordinates" -> obj.asInstanceOf[Point].getCoordinate.toJson
        )
      } else if (c == classOf[LineString]) {
        JsObject(
          "type"        -> JsString("LineString"),
          "coordinates" -> obj.asInstanceOf[LineString].getCoordinates.toJson
        )
      } else if (c == classOf[Polygon]) {
        JsObject(
          "type"        -> JsString("Polygon"),
          "coordinates" -> JsArray(obj.asInstanceOf[Polygon].getCoordinates.toJson)
        )
      } else if (c == classOf[MultiPoint]) {
        JsObject(
          "type"        -> JsString("MultiPoint"),
          "coordinates" -> obj.asInstanceOf[MultiPoint].getCoordinates.toJson
        )
      } else if (c == classOf[MultiLineString]) {
        val x = convertMultiLineString(obj.asInstanceOf[MultiLineString])
        JsObject(
          "type"        -> JsString("MultiLineString"),
          "coordinates" -> JsArray(x)
        )
      } else if (c == classOf[MultiPolygon]) {
        val x = convertMultiPolygon(obj.asInstanceOf[MultiPolygon])
        JsObject(
          "type"        -> JsString("MultiPolygon"),
          "coordinates" -> JsArray(x)
        )
      } else {
        throw new UnsupportedOperationException
      }
    }

    override def read(json: JsValue): Geometry = {
      val geometryFactory: GeometryFactory = new GeometryFactory()
      val fields = json.asJsObject.fields
      if (fields("type").equals(JsString("Point"))) {
        geometryFactory.createPoint(fields("coordinates").convertTo[Coordinate])
      } else if (fields("type").equals(JsString("LineString"))) {
        geometryFactory.createLineString(fields("coordinates").convertTo[Array[Coordinate]])
      } else if (fields("type").equals(JsString("Polygon"))) {
        val coordinates = readCoordinates(getCoordinates(getCoordinates(fields("coordinates"))(0)))
        geometryFactory.createPolygon(coordinates.toArray)
      } else if (fields("type").equals(JsString("MultiPoint"))) {
        geometryFactory.createMultiPoint(fields("coordinates").convertTo[Array[Coordinate]])
      } else if (fields("type").equals(JsString("MultiLineString"))) {
        val coords: Array[Array[Coordinate]] = fields("coordinates").convertTo[Array[Array[Coordinate]]]
        val linestrings: Array[LineString] = coords.map(coord => geometryFactory.createLineString(coord))
        geometryFactory.createMultiLineString(linestrings)
      } else if (fields("type").equals(JsString("MultiPolygon"))) {
        val coordsPolygons: Vector[JsValue] = getCoordinates(fields("coordinates"))
        val polygons: Array[Polygon] = new Array[Polygon](coordsPolygons.size)
        for (i <- 0 until coordsPolygons.size) {
          val y: Array[Coordinate] = coordsPolygons(i)
            .asInstanceOf[JsArray]
            .elements(0)
            .asInstanceOf[JsArray]
            .elements
            .map(el => el.convertTo[Coordinate])
            .toArray
          polygons(i) = geometryFactory.createPolygon(y)
        }
        geometryFactory.createMultiPolygon(polygons)
      } else throw new UnsupportedOperationException
    }

    def getCoordinates(jsonCoordinates: JsValue): Vector[JsValue] =
      jsonCoordinates.asInstanceOf[JsArray].elements

    def convertMultiPolygon(geom: Geometry): Vector[JsValue] = {
      val size = geom.getNumGeometries
      val polygons = new Array[Polygon](size)

      for (i <- 0 until size)
        polygons(i) = geom.getGeometryN(i).asInstanceOf[Polygon]
      polygons.map(polygon => JsArray(polygon.getCoordinates.toJson)).toVector
    }

    def convertMultiLineString(geom: Geometry): Vector[JsValue] = {
      val size = geom.getNumGeometries
      val linestrings = new Array[LineString](size)

      for (i <- 0 until size)
        linestrings(i) = geom.getGeometryN(i).asInstanceOf[LineString]
      linestrings.map(ls => ls.getCoordinates.toJson).toVector
    }

    def readCoordinates(coords: Vector[JsValue]): Vector[Coordinate] =
      coords.map(el => el.convertTo[Coordinate])
  }

  implicit val geometryFeatureFormat: RootJsonFormat[GeometryFeature] = jsonFormat3(GeometryFeature.apply)
  implicit val featureCollectionFormat: RootJsonFormat[FeatureCollection] = jsonFormat2(FeatureCollection.apply)

}
