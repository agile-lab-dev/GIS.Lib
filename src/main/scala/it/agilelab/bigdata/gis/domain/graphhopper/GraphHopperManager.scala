package it.agilelab.bigdata.gis.domain.graphhopper

import com.graphhopper.matching.{EdgeMatch, GPXExtension}
import com.graphhopper.util.details.PathDetailsBuilderFactory
import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.configuration.GraphHopperConfiguration
import it.agilelab.bigdata.gis.domain.exceptions.MatchedRouteError
import it.agilelab.bigdata.gis.domain.graphhopper.GraphHopperManager.getRouteTypesKm
import it.agilelab.bigdata.gis.domain.loader.RouteMatcher

import java.util
import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

case class GraphHopperManager(conf: Config) extends RouteMatcher with Logger {

  val graphConf: GraphHopperConfiguration = GraphHopperConfiguration(conf)

  def typeOfRoute(edge: EdgeMatch): String =
    Option(graphConf.encoder.getHighwayAsString(edge.getEdgeState))
      //TODO is this right?
      .getOrElse("unclassified")

  override def matchingRoute(gpsPoints: Seq[GPSPoint]): Either[MatchedRouteError, MatchedRoute] = {
    Try {
      val calcRoute = graphConf.mapMatching.doWork(gpsPoints.map(_.toGPXEntry))

      val length = calcRoute.getMatchLength
      val time = calcRoute.getMatchMillis
      val edges: Seq[EdgeMatch] = calcRoute.getEdgeMatches
      val routeTypesKm: Map[String, Double] = getRouteTypesKm(getMappedEdges(calcRoute.getEdgeMatches))
      val allEdges: Seq[EnrichEdge] = getAllEdges(edges)
      val points: Seq[TracePoint] = allEdges.filter(_.isInitialNode).map(_.node.get)

      if (calcRoute.getMergedPath.calcEdges().nonEmpty) {

        //retrieve distance for each pair of points
        val distances = calcRoute.getMergedPath
          .calcDetails(List("distance"), new PathDetailsBuilderFactory, 0)
          .get("distance")

        //union information on edge with information on distances
        val distancesBetweenEdge: Seq[EnrichEdgeWithDistance] =
          allEdges.zip(distances).map {
            case (edge, distance) =>
              EnrichEdgeWithDistance(
                edge.idNode,
                edge.isInitialNode,
                edge.typeOfRoute,
                edge.node,
                distance.getValue.asInstanceOf[Double]
              )
          }

        //retrieve index of only initialNode
        val idxInitialNode: Seq[Int] =
          distancesBetweenEdge.zipWithIndex
            .filter {
              case (edgeWithDistance, _) => edgeWithDistance.isInitialNode
            }
            .map {
              case (_, idx) => idx
            }

        // distances between node
        val distancesBetweenNode =
          idxInitialNode
            .zip(idxInitialNode.tail)
            .map {
              case (fromIdx, toIdx) =>
                val subList = distancesBetweenEdge.subList(fromIdx, toIdx + 1)

                val (typesOfRoute, _) =
                  subList
                    .map(_.typeOfRoute)
                    .groupBy(identity)
                    .mapValues(_.size)
                    .reduce((a, b) => if (a._2 > b._2) a else b)

                val distance =
                  subList
                    .foldLeft(0D)((acc, elem) => {
                      acc + elem.distance
                    })

                DistancePoint(
                  subList.head.node.get,
                  subList.last.node.get,
                  distance,
                  subList.last.node.get.time - subList.head.node.get.time,
                  typesOfRoute
                )
            }


        MatchedRoute(
          points,
          length,
          time,
          routeTypesKm,
          distancesBetweenNode
        )
      } else {
        if (points.nonEmpty) {
          MatchedRoute(points, length, time, routeTypesKm, Seq.empty)
        } else {
          MatchedRoute(gpsPoints.map(_.toTracePoint), length, time, routeTypesKm, Seq.empty)
        }
      }
    } match {
      case Success(route) => Right(route)
      case Failure(ex) =>
        logger.error("Failed to match route", ex)
        Left(MatchedRouteError(ex))
    }
  }

  private def getAllEdges(edges: Seq[EdgeMatch]) = {
    edges.toList.flatMap(edge => {
      val gpsExtensions: util.List[GPXExtension] = edge.getGpxExtensions
      val edgeId = edge.getEdgeState.getBaseNode

      if (gpsExtensions.size() == 0)
        List(
          EnrichEdge(edgeId, isInitialNode = false, typeOfRoute(edge), None)
        )
      else
        gpsExtensions.flatMap(item => {
          List(
            EnrichEdge(
              item.getQueryResult.getClosestNode,
              isInitialNode = true,
              typeOfRoute(edge),
              Some(
                TracePoint(
                  item.getEntry.lat,
                  item.getEntry.lon,
                  Some(item.getEntry.ele),
                  item.getEntry.getTime,
                  Some(item.getQueryResult.getSnappedPoint.lat),
                  Some(item.getQueryResult.getSnappedPoint.lon),
                  Some(item.getQueryResult.getSnappedPoint.ele),
                  Some(typeOfRoute(edge)),
                  Some(edge.getEdgeState.getName),
                  Some(graphConf.encoder.getSpeed(item.getQueryResult.getClosestEdge.getFlags).toInt),
                  Some(item.getQueryResult.getQueryDistance)
                )
              )
            )
          )
        })

    })
  }

  private def getMappedEdges(edges: Seq[EdgeMatch]) = {
    edges
      .map(edge => (graphConf.encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance))
      .map {
        case (null, value) => ("unclassified", value)
        case x: (String, Double) => x
      }
  }
}

object GraphHopperManager {

  private def getRouteTypesKm(mappedEdges: Seq[(String, Double)]) = {
    mappedEdges
      .groupBy(_._1)
      .map(x => (x._1, x._2.map(_._2).sum))
  }
}
