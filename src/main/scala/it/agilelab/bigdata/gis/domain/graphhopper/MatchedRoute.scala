package it.agilelab.bigdata.gis.domain.graphhopper

import com.graphhopper.util.GPXEntry

import scala.util.Try

/**
  * Created by stefano on 09/10/17.
  */
case class Point(lat: Double, lon: Double, time: Option[Long])

case class DistancePoint(node1: Point,
                         node2: Point,
                         distance: Double,
                         diffTime: Long)

case class MatchedRoute(points: Seq[GPXEntry],
                        length: Double,
                        time: Long,
                        routes: Map[String, Double],
                        distanceBetweenPoints: Seq[DistancePoint]) {

  def getKmType(s: String): Try[Double] = {
    Try.apply(routes(s))
  }

}
