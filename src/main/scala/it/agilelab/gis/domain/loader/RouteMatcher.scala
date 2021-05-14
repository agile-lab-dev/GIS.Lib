package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.exceptions.MatchedRouteError
import it.agilelab.gis.domain.graphhopper.{ GPSPoint, MatchedRoute }

trait RouteMatcher {

  def matchingRoute(gpsPoints: Seq[GPSPoint]): Either[MatchedRouteError, MatchedRoute]
}
