package it.agilelab.gis.domain.graphhopper

import com.graphhopper.util.Parameters

/** Supported algorithm
  */
sealed trait AlgorithmType { def value: String }

/** [[AlgorithmType]] companion object holds all supported algorithms.
  */
object AlgorithmType {
  case object DIJKSTRA_BI extends AlgorithmType { lazy val value: String = Parameters.Algorithms.DIJKSTRA_BI }
  case object DIJKSTRA extends AlgorithmType { lazy val value: String = Parameters.Algorithms.DIJKSTRA }
  case object DIJKSTRA_ONE_TO_MANY extends AlgorithmType {
    lazy val value: String = Parameters.Algorithms.DIJKSTRA_ONE_TO_MANY
  }
  case object ASTAR extends AlgorithmType { lazy val value: String = Parameters.Algorithms.ASTAR }
  case object ASTAR_BI extends AlgorithmType { lazy val value: String = Parameters.Algorithms.ASTAR_BI }
  case object ALT_ROUTE extends AlgorithmType { lazy val value: String = Parameters.Algorithms.ALT_ROUTE }
  case object ROUND_TRIP extends AlgorithmType { lazy val value: String = Parameters.Algorithms.ROUND_TRIP }

  val values = Seq(DIJKSTRA_BI, DIJKSTRA, DIJKSTRA_ONE_TO_MANY, ASTAR, ASTAR_BI, ALT_ROUTE, ROUND_TRIP)

  /** Parses source type from string */
  def fromValue(v: String): AlgorithmType =
    v match {
      case DIJKSTRA_BI.value          => DIJKSTRA_BI
      case DIJKSTRA.value             => DIJKSTRA
      case DIJKSTRA_ONE_TO_MANY.value => DIJKSTRA_ONE_TO_MANY
      case ASTAR.value                => ASTAR
      case ASTAR_BI.value             => ASTAR_BI
      case ALT_ROUTE.value            => ALT_ROUTE
      case ROUND_TRIP.value           => ROUND_TRIP
      case _ =>
        throw new IllegalArgumentException(
          s"$v is not a valid algorithm type, the algorithm supported are ${values.mkString(",")}")
    }
}
