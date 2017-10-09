package it.agilelab.bigdata.gis.grephhopper

import java.util
import scala.collection.JavaConversions._

import com.graphhopper.matching.{EdgeMatch, MatchResult}

/**
  * Created by stefano on 09/10/17.
  */
case class MatchedRoute(matchResult: MatchResult) {

  val length = matchResult.getMatchLength
  val time = matchResult.getMatchMillis

  private val edges: Seq[EdgeMatch] = matchResult.getEdgeMatches

  //edges.foreach( edge => {
  //    edge.getMinDistance
  //    edge.getEdgeState.getFlags

  //  }
 // )



}
