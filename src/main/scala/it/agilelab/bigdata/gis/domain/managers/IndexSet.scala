package it.agilelab.bigdata.gis.domain.managers

import it.agilelab.gis.domain.models.{ OSMBoundary, OSMHouseNumber, OSMStreetAndHouseNumber }
import it.agilelab.gis.domain.spatialList.GeometryList

case class IndexSet(
    boundaries: GeometryList[OSMBoundary],
    streets: GeometryList[OSMStreetAndHouseNumber],
    houseNumbers: GeometryList[OSMHouseNumber]
)
