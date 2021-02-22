package it.agilelab.bigdata.gis.domain.managers

import it.agilelab.bigdata.gis.domain.models.{OSMBoundary, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

case class IndexSet(boundaries: GeometryList[OSMBoundary],
                    regions: GeometryList[OSMBoundary],
                    streets: GeometryList[OSMStreetAndHouseNumber])
