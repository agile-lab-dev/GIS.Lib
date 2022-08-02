package it.agilelab.gis.domain.loader

import org.locationtech.jts.geom._

import scala.util.Try

/** *
  * Loader for Points of interest of type leisure
  */
class OSMPoiLeisureLoader extends OSMPoiLoader[OSMPoiLeisure] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiLeisure = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString | _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val leisure = Try(Option(fields(5)).map(_.toString).getOrElse("")).toOption
        OSMPoiLeisure(geometry, osmId, name = name, leisure = leisure)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val leisure = Try(Option(fields(6)).map(_.toString).getOrElse("")).toOption
        OSMPoiLeisure(geometry, osmId, name = name, leisure = leisure)
      case _ =>
        OSMPoiLeisure(geometry, osmId)
    }
  }

}
