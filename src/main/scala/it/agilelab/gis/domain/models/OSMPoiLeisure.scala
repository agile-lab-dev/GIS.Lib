package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

case class OSMPoiLeisure(
    geometry: Geometry,
    osmId: Option[String] = None,
    name: Option[String] = None,
    leisure: Option[String] = None
) extends OSMPoi(geometry, osmId, name) {

  override def toString: String =
    s"""Poi: ${geometry.toString}
       |osmId: ${osmId.getOrElse("")}
       |name: ${name.getOrElse("")}
       |leisure: ${leisure.getOrElse("")}
       """.stripMargin

}
