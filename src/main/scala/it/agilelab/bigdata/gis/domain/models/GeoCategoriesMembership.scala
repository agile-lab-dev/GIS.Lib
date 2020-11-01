package it.agilelab.bigdata.gis.domain.models

import com.vividsolutions.jts.geom.{Geometry, MultiPolygon, Polygon}

case class OSMGeoCategory(label: String, geometa: OSMGeoMetadata, multiPolygon: Geometry) extends MultiPolygon(
  {
    val length = multiPolygon.getNumGeometries
    (0 until length).map{
      x => multiPolygon.getGeometryN(x).asInstanceOf[Polygon]
    }
    }.toArray,
  multiPolygon.getFactory
)

case class OSMGeoMetadata(map: Map[String, AnyRef], description: String)

sealed trait InputCategory
object InputCategory {
  case object Country extends InputCategory
  case class Custom(label: String) extends InputCategory
}

case class CategoryMembershipOutput(categoryLabel: String, geometryLabel: String, metadata: Map[String, AnyRef])
