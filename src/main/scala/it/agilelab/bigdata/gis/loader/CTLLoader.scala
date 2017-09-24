package it.agilelab.bigdata.gis.loader

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory
import it.agilelab.bigdata.gis.enums.IndexType
import it.agilelab.bigdata.gis.models.{HereMapsStreet, HereMapsStreetType, OSMStreet, OSMStreetType}
import it.agilelab.bigdata.gis.spatialList._

import scala.io.Source

/**
  * Created by paolo on 12/09/2017.
  */
object CTLLoader{

  //Pay attention to side effects

  var index: GeometryList[HereMapsStreet] = null
  def getStreetIndex(path: String) = {
    if (index == null){
      index = new CTLLoader(7).loadIndex(path)
    }
    index
  }

}

class CTLLoader(geometryPosition: Int) extends Loader[HereMapsStreet]{

  val separator = """\|\|"""
  val geoSeparator = ';'
  val openStep = '('
  val closeStep = ')'

  val sridFactory8003 = new GeometryFactory(new PrecisionModel(), 8003, CoordinateArraySequenceFactory.instance())

  protected def streetMapping(fields: Array[AnyRef],line: Geometry): HereMapsStreet = {

    val fields2 = fields.map(_.toString)

    val streetType: String = fields2(4)
    val st = streetType match {
      case "\"1\"" => HereMapsStreetType.Motorway
      case "\"2\"" => HereMapsStreetType.ExtraUrban
      case "\"3\"" => HereMapsStreetType.Area1_Large
      case "\"4\"" => HereMapsStreetType.Area2_Medium
      case "\"5\"" => HereMapsStreetType.Area3_Small
      case _ => HereMapsStreetType.Unknown
    }

    val length: Double = fields2(8).replace(',', '.').toDouble
    var bidirected: Boolean = false
    if (fields2(10) == "\"Y\"") bidirected = true
    val street: String = fields2(11)
    val city: String = fields2(12)
    val county: String = fields2(13)
    val state: String = fields2(14)
    val country: String = fields2(15)
    val fromSpeed: Integer = fields2(16).toInt
    val toSpeed: Integer = fields2(17).toInt

    HereMapsStreet(line, street, city, county, state, country, Math.max(fromSpeed, toSpeed), bidirected, length, st)
  }




  override def loadFile(source: String): Iterator[(Array[AnyRef],Geometry)] = {

    var data = false
    val reader = Source.fromFile(source,"UTF-8")


    reader.getLines.flatMap(line => {
      val ls: Option[(Array[AnyRef], Geometry)] = if (!data) {
        data = if (line == "BEGINDATA") true else false
        Option.empty[(Array[AnyRef],Geometry)]
      } else {

        val fields: Array[String] = line.split(separator)
        if(fields.size == 0){
          println("bad splitting")
          Option.empty[(Array[AnyRef],LineString)]
        }else {
          val geometry = fields(geometryPosition).toString
          val lineString = buildGeometry(geometry)
          lineString.map(ls => (fields.map(_.asInstanceOf[AnyRef]), ls))
        }
      }
      ls
    })
  }

  def buildGeometry(geoStr: String): Option[Geometry] = {
    val fields = parseGeometry(geoStr)
    if(fields.size == 205){
      Some(buildGeometry(fields))
    }else{
      println(fields.size)
      None
    }
  }

  def buildGeometry(fields: Array[String]): Geometry = {
    val geotype = fields(0)
    if(geotype == "2002") {
      val lonlat = fields.slice(105, fields.length)
      val lonlatGood = lonlat.filterNot(_.isEmpty)
      val coordinates: Iterator[Coordinate] = lonlatGood.sliding(2, 2).map(ll => new Coordinate(ll(0).replace(',', '.').toDouble, ll(1).replace(',', '.').toDouble))

      sridFactory8003.createLineString(coordinates.toArray)
    }else{
      throw new NotImplementedError("Only lines are handled")
    }
  }

  def parseGeometry(geoStr: String): Array[String] = {
    if(geoStr.head == openStep){
      parseGeometry(geoStr.tail)
    }else{
      val cleanGeoStr = if(geoStr.head == closeStep) geoStr.tail else geoStr
      val sepPos = cleanGeoStr.indexOf(geoSeparator)
      if(sepPos == -1) {
        val closePos = cleanGeoStr.indexOf(closeStep)
        if (closePos != -1) {
          val newField = cleanGeoStr.substring(0, closePos)
          Array(newField)
        }else{
          Array.empty[String]
        }
      }
      else {
        val newField = cleanGeoStr.substring(0, sepPos)
        val otherPart = cleanGeoStr.substring(sepPos + 2)
        parseGeometry(otherPart).+:(newField)
      }
    }

  }

  def parseGeoField(geoStr: String) = {
    val geoField = geoStr.substring(1,geoStr.size-2)
    //val geoFields = geoField.split(geoSeparator,-1)
  }
}
