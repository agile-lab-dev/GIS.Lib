package it.agilelab.bigdata.gis.domain

package object exceptions {

  /**
  * Reverse geocoding error.
  *
  * @param ex error cause.
  */
  case class ReverseGeocodingError(ex: Throwable)

  /**
  * Matcher route error.
  *
  * @param ex error reason.
  */
  case class MatchedRouteError(ex: Throwable)
}
