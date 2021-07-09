package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models.{ OSMBoundary, OSMHouseNumber, OSMStreetAndHouseNumber }
import it.agilelab.gis.domain.spatialList.GeometryList

/** [[IndexSet]] holds all indices for [[OSMManager]] used for reverse geocoding.
  * @param boundaries boundaries geometry
  * @param regions region geometry
  * @param streets streets geometry
  * @param houseNumbers house numbers geometry
  */
case class IndexSet(
    boundaries: GeometryList[OSMBoundary],
    regions: GeometryList[OSMBoundary],
    streets: GeometryList[OSMStreetAndHouseNumber],
    houseNumbers: GeometryList[OSMHouseNumber]
)
