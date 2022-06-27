package it.agilelab.gis.domain

package object exceptions {

  /** Reverse geocoding error.
    *
    * @param ex error cause.
    */
  case class ReverseGeocodingError(ex: Throwable)

  /** Matcher route error.
    *
    * @param ex error reason.
    */
  case class MatchedRouteError(ex: Throwable)

  sealed trait GeoRelationError

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
