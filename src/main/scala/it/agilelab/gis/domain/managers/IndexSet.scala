package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models.{ OSMBoundary, OSMHouseNumber, OSMStreetAndHouseNumber }
import it.agilelab.gis.domain.spatialList.GeometryList

case class IndexSet(
    boundaries: GeometryList[OSMBoundary],
    regions: GeometryList[OSMBoundary],
    streets: GeometryList[OSMStreetAndHouseNumber],
    houseNumbers: GeometryList[OSMHouseNumber]
)
