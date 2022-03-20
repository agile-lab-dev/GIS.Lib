package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models.OSMBoundary
import it.agilelab.gis.domain.spatialList.GeometryList
import org.locationtech.jts.geom.{ OSMHouseNumber, OSMStreetAndHouseNumber }

/** [[GeocodeIndexSet]] holds all indices for [[GeocodeManager]].
  *
  * @param boundaries boundaries geometry
  * @param streets streets geometry
  * @param houseNumbers house numbers geometry
  */
case class GeocodeIndexSet(
    boundaries: GeometryList[OSMBoundary],
    streets: GeometryList[OSMStreetAndHouseNumber],
    houseNumbers: GeometryList[OSMHouseNumber]
)
