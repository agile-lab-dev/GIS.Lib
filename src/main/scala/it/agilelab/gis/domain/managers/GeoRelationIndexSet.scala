package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models._
import it.agilelab.gis.domain.spatialList.GeometryList

/** [[GeoRelationIndexSet]] holds all indices for [[GeoRelationManager]].
  *
  * @param railways optional railways geometry
  * @param sea optional sea geometry
  */
case class GeoRelationIndexSet(
    railways: Option[GeometryList[OSMRailTrack]],
    sea: Option[GeometryList[OSMSea]]
)
