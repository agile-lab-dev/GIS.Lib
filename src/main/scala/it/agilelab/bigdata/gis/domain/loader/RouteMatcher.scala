package it.agilelab.bigdata.gis.domain.loader

import it.agilelab.bigdata.gis.domain.graphhopper.{GPSPoint, MatchedRoute}

trait RouteMatcher {

  def matchingRoute(gpsPoints: Seq[GPSPoint]): MatchedRoute
}
