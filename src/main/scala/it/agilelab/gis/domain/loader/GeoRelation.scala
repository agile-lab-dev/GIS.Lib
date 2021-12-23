package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.exceptions.{ InsideSeaError, NearestRailwayError }
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.models.{ InsideSeaResponse, NearestRailwayResponse }

/** Represents operations related to relations among objects
  *
  * @author Agile Lab s.r.l.
  */
trait GeoRelation {
  def nearestRailway(point: IdentifiableGPSPoint): Either[NearestRailwayError, NearestRailwayResponse]
  def isInsideSea(point: IdentifiableGPSPoint): Either[InsideSeaError, InsideSeaResponse]
}
