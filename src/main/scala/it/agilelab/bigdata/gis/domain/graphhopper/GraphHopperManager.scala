package it.agilelab.bigdata.gis.domain.graphhopper

import java.util

import com.graphhopper.matching.{EdgeMatch, GPXExtension, MatchResult}
import com.graphhopper.routing.profiles.{FactorizedDecimalEncodedValue, SimpleIntEncodedValue}
import com.graphhopper.util.details.PathDetailsBuilderFactory
import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.loader.{GraphHopperConfiguration, RouteMatcher}

import scala.collection.JavaConversions._

case class GraphHopperManager(val conf: Config) extends RouteMatcher with Logger {

  val graphConf: GraphHopperConfiguration = GraphHopperConfiguration(conf)

  val speedEncoder: FactorizedDecimalEncodedValue = graphConf.encoder.getEncodedValue("car.average_speed", classOf[FactorizedDecimalEncodedValue])
  val highwayEncoder: SimpleIntEncodedValue = graphConf.encoder.getEncodedValue("car.highway", classOf[SimpleIntEncodedValue])

  def typeOfRoute(edge: EdgeMatch): String =
    graphConf.encoder.getHighwayAsString(edge.getEdgeState) match {
      case null => "unclassified" //todo is right this
      case value: String => value
    }

  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  override def matchingRoute(gpsPoints: Seq[GPSPoint]): MatchedRoute = {

    val calcRoute: MatchResult = graphConf.mapMatching.doWork(gpsPoints.map(_.toGPXEntry))

    val length = calcRoute.getMatchLength
    val time = calcRoute.getMatchMillis
    val edges: Seq[EdgeMatch] = calcRoute.getEdgeMatches

    if (calcRoute.getMergedPath.calcEdges().nonEmpty) {

      val mappedEdges: Seq[(String, Double)] =
        edges
          .map(edge =>
            (graphConf.encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance)
          )
          .map {
            case (null, value) => ("unclassified", value)
            case x: (String, Double) => x
          }

      //retrieve distance for each pair of points
      val distances = calcRoute.getMergedPath
        .calcDetails(List("distance"), new PathDetailsBuilderFactory, 0)
        .get("distance")

      //retrieve all edges
      val alledges: Seq[EnrichEdge] = edges.toList.flatMap(edge => {
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
                    Some(speedEncoder.getDecimal(false, item.getQueryResult.getClosestEdge.getFlags).toInt),
                    Some(item.getQueryResult.getQueryDistance)
                  )
                )
              )
            )
          })

      })

      //union information on edge with information on distances
      val distancesBetweenEdge: Seq[EnrichEdgeWithDistance] =
        alledges.zip(distances).map {
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
            case (edgeWithDistance, idx) => edgeWithDistance.isInitialNode
          }
          .map {
            case (edgeWithDistance, idx) => idx
          }

      // distances between node
      val distancesBetweenNode =
        idxInitialNode
          .zip(idxInitialNode.tail)
          .map {
            case (fromIdx, toIdx) =>
              val subList = distancesBetweenEdge.subList(fromIdx, toIdx + 1)

              val (typesOfRoute, nrOccurrences) =
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

      val routeTypesKm: Map[String, Double] =
        mappedEdges
          .groupBy(_._1)
          .map(x => (x._1, x._2.map(_._2).sum))

      val points: Seq[TracePoint] =
        alledges.filter(_.isInitialNode).map(_.node.get)

      MatchedRoute(
        points,
        length,
        time,
        Map.empty[String, Double],
//        routeTypesKm,
        distancesBetweenNode
      )
    } else
      MatchedRoute(gpsPoints.map(_.toTracePoint), length, time, Map.empty, Seq.empty)
  }
}
