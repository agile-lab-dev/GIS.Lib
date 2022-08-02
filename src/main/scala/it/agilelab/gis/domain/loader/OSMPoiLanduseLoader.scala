package it.agilelab.gis.domain.loader

import org.locationtech.jts.geom._

import scala.util.Try

/** *
  * Loader for Points of interest of type landuse
  */
class OSMPoiLanduseLoader extends OSMPoiLoader[OSMPoiLanduse] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiLanduse = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString | _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val landuse = Try(Option(fields(4)).map(_.toString).getOrElse("")).toOption
        OSMPoiLanduse(geometry, osmId, name = name, landuse = landuse)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val landuse = Try(Option(fields(5)).map(_.toString).getOrElse("")).toOption
        OSMPoiLanduse(geometry, osmId, name = name, landuse = landuse)
      case _ =>
        OSMPoiLanduse(geometry, osmId)
    }
  }

}
