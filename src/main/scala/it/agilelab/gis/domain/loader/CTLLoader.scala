package it.agilelab.gis.domain.loader

import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.domain.spatialList.GeometryList
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory
import org.locationtech.jts.geom.{
  Coordinate,
  Geometry,
  GeometryFactory,
  HereMapsStreet,
  HereMapsStreetType,
  LineString,
  PrecisionModel
}

import scala.io.Source

/** Created by paolo on 12/09/2017.
  */
object CTLLoader {

  //Pay attention to side effects

  var index: GeometryList[HereMapsStreet] = _
  def getStreetIndex(path: String): GeometryList[HereMapsStreet] = {
    if (index == null) {
      index = new CTLLoader(7).loadIndex(path)
    }
    index
  }

}

class CTLLoader(geometryPosition: Int) extends Loader[HereMapsStreet] {

  val separator = """\|\|"""
  val geoSeparator = ';'
  val openStep = '('
  val closeStep = ')'

  val sridFactory8003 = new GeometryFactory(new PrecisionModel(), 8003, CoordinateArraySequenceFactory.instance())

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): HereMapsStreet = {

    val stringFields = fields.map(_.toString)

    val streetType: String = stringFields(4)
    val st = HereMapsStreetType.fromValue(streetType)

    val length: Double = stringFields(8).replace(',', '.').toDouble
    var biDirected: Boolean = false
    if (stringFields(10) == "\"Y\"") biDirected = true
    val street: String = stringFields(11)
    val city: String = stringFields(12)
    val county: String = stringFields(13)
    val state: String = stringFields(14)
    val country: String = stringFields(15)
    val fromSpeed: Integer = stringFields(16).toInt
    val toSpeed: Integer = stringFields(17).toInt

    HereMapsStreet(line, street, city, county, state, country, Math.max(fromSpeed, toSpeed), biDirected, length, st)
  }

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    var data = false
    val reader = Source.fromFile(source, "UTF-8")

    reader.getLines.flatMap { line =>
      val ls: Option[(Array[AnyRef], Geometry)] = if (!data) {
        data = if (line == "BEGINDATA") true else false
        Option.empty[(Array[AnyRef], Geometry)]
      } else {

        val fields: Array[String] = line.split(separator)
        if (fields.length == 0) {
          println("bad splitting")
          Option.empty[(Array[AnyRef], LineString)]
        } else {
          val geometry = fields(geometryPosition)
          val lineString = buildGeometry(geometry)
          lineString.map(ls => (fields.map(_.asInstanceOf[AnyRef]), ls))
        }
      }
      ls
    }
  }

  def buildGeometry(geoStr: String): Option[Geometry] = {
    val fields = parseGeometry(geoStr)

    if (fields.length == 205) {
      Some(buildGeometry(fields))
    } else {
      println(fields.length)
      None
    }
  }

  def buildGeometry(fields: Array[String]): Geometry = {
    val geotype = fields(0)
    if (geotype == "2002") {
      val lonlat = fields.slice(105, fields.length)
      val lonlatGood = lonlat.filterNot(_.isEmpty)
      val coordinates: Iterator[Coordinate] = lonlatGood
        .sliding(2, 2)
        .map(ll => new Coordinate(ll(0).replace(',', '.').toDouble, ll(1).replace(',', '.').toDouble))

      sridFactory8003.createLineString(coordinates.toArray)
    } else {
      throw new NotImplementedError("Only lines are handled")
    }
  }

  def parseGeometry(geoStr: String): Array[String] =
    if (geoStr.head == openStep) {
      parseGeometry(geoStr.tail)
    } else {
      val cleanGeoStr = if (geoStr.head == closeStep) geoStr.tail else geoStr
      val sepPos = cleanGeoStr.indexOf(geoSeparator)
      if (sepPos == -1) {
        val closePos = cleanGeoStr.indexOf(closeStep)
        if (closePos != -1) {
          val newField = cleanGeoStr.substring(0, closePos)
          Array(newField)
        } else {
          Array.empty[String]
        }
      } else {
        val newField = cleanGeoStr.substring(0, sepPos)
        val otherPart = cleanGeoStr.substring(sepPos + 2)
        parseGeometry(otherPart).+:(newField)
      }
    }

}
