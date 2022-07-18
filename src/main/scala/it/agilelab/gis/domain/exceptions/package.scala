package it.agilelab.gis.domain

import it.agilelab.gis.domain.graphhopper.GPSPoint

package object exceptions {

  /** Matcher route error.
    */
  sealed trait MatchedRouteError {
    val ex: Throwable
  }

  sealed trait GeoRelationError

  /** Reverse geocoding error.
    *
    * @param ex error cause.
    */
  case class ReverseGeocodingError(ex: Throwable)

  /** Recoverable broken sequence error
    *
    * @param ex error reason.
    * @param observation the point that caused the error
    */
  case class RecoverableBrokenSequenceRouteError(ex: Throwable, observation: GPSPoint) extends MatchedRouteError

  /** Not recoverable broken sequence error
    *
    * @param ex error reason.
    */
  case class NotRecoverableBrokenSequenceRouteError(ex: Throwable) extends MatchedRouteError

  /** Generic map matching error
    *
    * @param ex error reason.
    */
  case class GenericMatchedRouteError(ex: Throwable) extends MatchedRouteError

  /** Railways distance error.
    *
    * @param ex error reason.
    */
  case class NearestRailwayError(ex: Throwable) extends GeoRelationError

  /** Inside sea error.
    *
    * @param ex error reason.
    */
  case class InsideSeaError(ex: Throwable) extends GeoRelationError

  /** Point of interest search error
    *
    * @param ex error reason.
    */
  case class PoiSearchError(ex: Throwable)
}
