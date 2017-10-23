package it.agilelab.bigdata.gis.graphhopper

import com.graphhopper.util.GPXEntry

import scala.util.Try

/**
  * Created by stefano on 09/10/17.
  */
case class MatchedRoute(points: Seq[GPXEntry],length: Double, time: Long, routes: Map[String, Double]) {

  def getKmType(s: String): Try[Double] = {
    Try(routes(s))
  }


}