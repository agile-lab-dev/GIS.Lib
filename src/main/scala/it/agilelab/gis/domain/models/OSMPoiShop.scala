package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

case class OSMPoiShop(
    geometry: Geometry,
    osmId: Option[String] = None,
    name: Option[String] = None,
    shop: Option[String] = None
) extends OSMPoi(geometry, osmId, name) {

  override def toString: String =
    s"""Poi: ${geometry.toString}
       |osmId: ${osmId.getOrElse("")}
       |name: ${name.getOrElse("")}
       |shop: ${shop.getOrElse("")}
       """.stripMargin

}
