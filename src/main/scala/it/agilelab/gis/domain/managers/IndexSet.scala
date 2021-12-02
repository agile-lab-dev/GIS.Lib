package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models.{ OSMBoundary, OSMHouseNumber, OSMRailTrack, OSMStreetAndHouseNumber }
import it.agilelab.gis.domain.spatialList.GeometryList

/** [[IndexSet]] holds all indices for [[OSMManager]].
  * @param boundaries boundaries geometry
  * @param streets streets geometry
  * @param houseNumbers house numbers geometry
  * @param railways optional railways geometry
  */
case class IndexSet(
    boundaries: GeometryList[OSMBoundary],
    streets: GeometryList[OSMStreetAndHouseNumber],
    houseNumbers: GeometryList[OSMHouseNumber],
    railways: Option[GeometryList[OSMRailTrack]]
)
