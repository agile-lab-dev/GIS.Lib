package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.{ Geometry, MultiLineString, MultiPolygon, Point }
import it.agilelab.gis.domain.models.OSMPoiLeisure

import scala.util.Try

/** *
  * Loader for Points of interest of type leisure
  */
class OSMPoiLeisureLoader extends OSMPoiLoader[OSMPoiLeisure] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiLeisure = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val leisure = getInfosFromOtherTags(fields, 9, "leisure")
        OSMPoiLeisure(geometry, osmId, name = name, leisure = leisure)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val leisure = Try(Option(fields(16)).map(_.toString).getOrElse("")).toOption
        OSMPoiLeisure(geometry, osmId, name = name, leisure = leisure)
      case _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val leisure = getInfosFromOtherTags(fields, 10, "leisure")
        OSMPoiLeisure(geometry, osmId, name = name, leisure = leisure)
      case _ =>
        OSMPoiLeisure(geometry, osmId)
    }
  }

}
