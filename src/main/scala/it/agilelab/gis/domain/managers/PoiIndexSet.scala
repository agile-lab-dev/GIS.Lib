package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.spatialList.GeometryList
import org.locationtech.jts.geom._

/** [[PoiIndexSet]] holds all indices for [[PoiManager]].
  *
  * @param amenity amenity geometry
  * @param landuse landuse geometry
  * @param leisure leisure geometry
  * @param natural natural geometry
  * @param shop shop geometry
  */
case class PoiIndexSet(
    amenity: GeometryList[OSMPoiAmenity],
    landuse: GeometryList[OSMPoiLanduse],
    leisure: GeometryList[OSMPoiLeisure],
    natural: GeometryList[OSMPoiNatural],
    shop: GeometryList[OSMPoiShop]
)
