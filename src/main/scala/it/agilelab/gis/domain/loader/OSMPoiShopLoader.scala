package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.{ Geometry, MultiLineString, MultiPolygon, Point }
import it.agilelab.gis.domain.models.OSMPoiShop

import scala.util.Try

/** *
  * Loader for Points of interest of type shop
  */
class OSMPoiShopLoader extends OSMPoiLoader[OSMPoiShop] {

  override protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMPoiShop = {
    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    geometry match {
      case _: MultiLineString | _: Point =>
        val name = Try(Option(fields(2)).map(_.toString).getOrElse("")).toOption
        val shop = Try(Option(fields(7)).map(_.toString).getOrElse("")).toOption
        OSMPoiShop(geometry, osmId, name = name, shop = shop)
      case _: MultiPolygon =>
        val name = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
        val shop = Try(Option(fields(8)).map(_.toString).getOrElse("")).toOption
        OSMPoiShop(geometry, osmId, name = name, shop = shop)
      case _ =>
        OSMPoiShop(geometry, osmId)
    }
  }

}
