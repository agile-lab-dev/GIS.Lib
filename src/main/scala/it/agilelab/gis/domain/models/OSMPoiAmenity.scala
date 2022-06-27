package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

case class OSMPoiAmenity(
    geometry: Geometry,
    osmId: Option[String] = None,
    name: Option[String] = None,
    amenity: Option[String] = None
) extends OSMPoi(geometry, osmId, name) {

  override def toString: String =
    s"""Poi: ${geometry.toString}
       |osmId: ${osmId.getOrElse("")}
       |name: ${name.getOrElse("")}
       |amenity: ${amenity.getOrElse("")}
       """.stripMargin

}
