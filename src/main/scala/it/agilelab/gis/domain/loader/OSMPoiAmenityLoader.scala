package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.{ Geometry, MultiLineString, MultiPolygon, Point }
import it.agilelab.gis.domain.models.OSMPoiAmenity

import scala.util.Try

/** *
  * Loader for Points of interest of type amenity
  */
class OSMPoiAmenityLoader extends OSMPoiLoader[OSMPoiAmenity] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiAmenity = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val amenity = getInfosFromOtherTags(fields, 9, "amenity")
        OSMPoiAmenity(geometry, osmId, name = name, amenity = amenity)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val amenity = Try(Option(fields(6)).map(_.toString).getOrElse("")).toOption
        OSMPoiAmenity(geometry, osmId, name = name, amenity = amenity)
      case _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val amenity = getInfosFromOtherTags(fields, 10, "amenity")
        OSMPoiAmenity(geometry, osmId, name = name, amenity = amenity)
      case _ =>
        OSMPoiAmenity(geometry, osmId)
    }
  }

}
