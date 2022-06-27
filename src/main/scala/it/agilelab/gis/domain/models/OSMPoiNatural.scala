package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

case class OSMPoiNatural(
    geometry: Geometry,
    osmId: Option[String] = None,
    name: Option[String] = None,
    natural: Option[String] = None
) extends OSMPoi(geometry, osmId, name) {

  override def toString: String =
    s"""Poi: ${geometry.toString}
       |osmId: ${osmId.getOrElse("")}
       |name: ${name.getOrElse("")}
       |natural: ${natural.getOrElse("")}
       """.stripMargin

}
