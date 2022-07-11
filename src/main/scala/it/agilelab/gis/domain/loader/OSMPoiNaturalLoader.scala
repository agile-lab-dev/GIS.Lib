package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.{ Geometry, MultiLineString, MultiPolygon, Point }
import it.agilelab.gis.domain.models.OSMPoiNatural

import scala.util.Try

/** *
  * Loader for Points of interest of type natural
  */
class OSMPoiNaturalLoader extends OSMPoiLoader[OSMPoiNatural] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiNatural = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString | _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val natural = Try(Option(fields(6)).map(_.toString).getOrElse("")).toOption
        OSMPoiNatural(geometry, osmId, name = name, natural = natural)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val natural = Try(Option(fields(7)).map(_.toString).getOrElse("")).toOption
        OSMPoiNatural(geometry, osmId, name = name, natural = natural)
      case _ =>
        OSMPoiNatural(geometry, osmId)
    }
  }

}
