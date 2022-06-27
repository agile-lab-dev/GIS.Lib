package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.{ Geometry, MultiLineString, MultiPolygon, Point }
import it.agilelab.gis.domain.models.OSMPoiLanduse

import scala.util.Try

/** *
  * Loader for Points of interest of type landuse
  */
class OSMPoiLanduseLoader extends OSMPoiLoader[OSMPoiLanduse] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiLanduse = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val landuse = getInfosFromOtherTags(fields, 9, "landuse")
        OSMPoiLanduse(geometry, osmId, name = name, landuse = landuse)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val landuse = Try(Option(fields(15)).map(_.toString).getOrElse("")).toOption
        OSMPoiLanduse(geometry, osmId, name = name, landuse = landuse)
      case _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val landuse = getInfosFromOtherTags(fields, 10, "landuse")
        OSMPoiLanduse(geometry, osmId, name = name, landuse = landuse)
      case _ =>
        OSMPoiLanduse(geometry, osmId)
    }
  }

}
