package it.agilelab.gis.domain.graphhopper

import com.graphhopper.matching.{ EdgeMatch, GPXExtension, MatchResult }
import com.graphhopper.util.details._
import com.graphhopper.util.{ DistancePlaneProjection, EdgeIteratorState }
import com.typesafe.config.Config
import it.agilelab.gis.core.encoder.CarFlagEncoderEnrich
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.configuration.GraphHopperConfiguration
import it.agilelab.gis.domain.exceptions.{
  GenericMatchedRouteError,
  MatchedRouteError,
  NotRecoverableBrokenSequenceRouteError,
  RecoverableBrokenSequenceRouteError
}
import it.agilelab.gis.domain.graphhopper.GraphHopperManager._
import it.agilelab.gis.domain.loader.RouteMatcher

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{ Failure, Success, Try }

/** GraphHopperManager is an implementation of a RouteMatcher.
  * @param conf configuration for this [[GraphHopperManager]], see [[GraphHopperConfiguration]] for more information.
  */
case class GraphHopperManager(conf: Config) extends RouteMatcher with Logger {

  private val graphConf: GraphHopperConfiguration = GraphHopperConfiguration(conf)

  override def matchingRoute(gpsPoints: Seq[GPSPoint]): Either[MatchedRouteError, MatchedRoute] =
    Try {
      val calcRoute: MatchResult = graphConf.mapMatching.doWork(gpsPoints.map(_.toGPXEntry).asJava)

      val length = calcRoute.getMatchLength
      val time = calcRoute.getMatchMillis
      val routeTypesKm: Map[String, Double] = getRouteTypesKm(getMappedEdges(calcRoute.getEdgeMatches.asScala))
      val edges: List[Edge] = getEdges(calcRoute.getEdgeMatches.asScala)
      val points: Seq[TracePoint] = getTracePoints(edges, graphConf.encoder)

      if (!calcRoute.getMergedPath.calcEdges().isEmpty) {
        MatchedRoute(
          points,
          Some(length),
          Some(time),
          routeTypesKm,
          distanceBetweenNodes(calcRoute, points, edges)
        )
      } else {
        if (points.nonEmpty) {
          // In this case the result is an empty set of edges and non empty set of points, so we're handling the case of
          // having coordinates that are near each other and the distance between them is less then
          // measurement_error_sigma, so we set the distance between each point to 0.
          // TODO can we use the haversine distance formula to be more accurate rather than setting it to 0?
          MatchedRoute(
            points,
            Some(length),
            Some(time),
            routeTypesKm,
            points.zip(points.tail).map { case (p, n) =>
              DistancePoint(
                node1 = p,
                node2 = n,
                distance = Some(0),
                diffTime = Math.abs(p.time - n.time),
                typeOfRoute = None
              )
            }
          )
        } else {
          MatchedRoute(filterEntries(gpsPoints).map(_.toTracePoint), None, None, routeTypesKm, Seq.empty)
        }
      }
    } match {
      case Success(route) => Right(route)
      case Failure(ex) if ex.getMessage.startsWith("Sequence is broken for submitted track") =>
        logger.error("Failed to match route due to a broken sequence", ex)
        val oPoint = extractObservationPoint(ex.getMessage)
        oPoint match {
          case Some(point) => Left(RecoverableBrokenSequenceRouteError(ex, point))
          case None        => Left(NotRecoverableBrokenSequenceRouteError(ex))
        }
      case Failure(ex) =>
        logger.error("Failed to match route", ex)
        Left(GenericMatchedRouteError(ex))
    }

  /** Get distances between every pair of neighbour point of the trip.
    * @param calcRoute match result
    * @param points trace points
    * @param edges edges
    * @return all distances between points
    */
  private def distanceBetweenNodes(
      calcRoute: MatchResult,
      points: Seq[TracePoint],
      edges: Seq[Edge]
  ): Seq[DistancePoint] = {
    // retrieve details for each pair of points
    val details = calcRoute.getMergedPath
      .calcDetails(
        List("details").asJava,
        new CustomPathDetailsBuilderFactory(typeOfRouteFunc = edge => typeOfRoute(edge, graphConf.encoder)),
        0)
      .get("details")
      .asScala

    // Assign at each edge a set of details, so that we know how long the edge is.
    //
    // edges    -------- edge 1 --------- -------- edge 2 --------- -------- edge 3 ---------
    // details  [d1, d2               dk] [dk+1, dk+1           dn]
    //
    // Note: It's possible for a subset of edges at the tail of the list to be long 0 meters.
    val distances: Seq[mutable.Buffer[PathDetail]] = edges.tail.zipWithIndex
      .map { case (edge, edgeIdx) =>
        val item = edge.item
        val (edgeId, baseNode, adjNode) = if (item.isOnDirectedEdge) {
          (
            item.getOutgoingVirtualEdge.getEdge,
            item.getOutgoingVirtualEdge.getBaseNode,
            item.getOutgoingVirtualEdge.getAdjNode
          )
        } else {
          (
            item.getQueryResult.getClosestEdge.getEdge,
            item.getQueryResult.getClosestEdge.getBaseNode,
            item.getQueryResult.getClosestEdge.getAdjNode
          )
        }

        val edgeIdxInDetails = if (edgeIdx == edges.length - 2) {
          // The last edge gets all the remaining details regardless of the actual edge.
          details.length
        } else {
          details.indexWhere { p =>
            val pDetails = p.getValue.asInstanceOf[CustomPathDetails]
            pDetails.edgeId == edgeId && pDetails.baseNode == baseNode && pDetails.adjNode == adjNode
          }
        }

        // When there is no corresponding edge, we aggregate the remaining details in the current edge.
        val idx: Int = if (edgeIdxInDetails < 0) 0 else edgeIdxInDetails
        val path = details.take(idx)
        details.remove(0, idx)
        path
      }

    val finalDistances = if (distances.length >= points.length) {
      // We expected that |distances|+1 == |points| but sometimes that relationship doesn't hold and we have to
      // aggregate single details into the previous group of details.
      // This algorithm is based on example outputs we've seen, so it might not catch all possible cases, see associated
      // tests for examples.
      val tailN = distances.length - points.length + 2
      distances.take(distances.length - tailN) :+ distances.takeRight(tailN).reduce(_ ++ _)
    } else {
      distances
    }

    points.zip(points.tail).zip(finalDistances).map { case ((p1, p2), d) =>
      DistancePoint(
        node1 = p1,
        node2 = p2,
        distance = if (d.nonEmpty) Some(d.map(_.getValue.asInstanceOf[CustomPathDetails].distance).sum) else None,
        diffTime = p2.time - p1.time,
        typeOfRoute = d.flatMap(_.getValue.asInstanceOf[CustomPathDetails].typeOfRoute) match {
          case routes if routes.isEmpty => None
          case routes                   => emptyToNone(routes.maxBy(_.length))
        }
      )
    }
  }

  private def getMappedEdges(edges: Seq[EdgeMatch]): Seq[EdgePair] =
    edges.map(edge => (graphConf.encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance))

  private def filterEntries(entries: Seq[GPSPoint]): Seq[GPSPoint] =
    entries.foldLeft(new ArrayBuffer[GPSPoint]()) { case (acc, elem) =>
      if (acc.nonEmpty && calcDistance(acc.last, elem) < graphConf.settings.measurementErrorSigma) {
        acc
      } else {
        acc :+ elem
      }
    }

  private def extractObservationPoint(s: String): Option[GPSPoint] = {
    val pattern = """observation:(\S+),(\S+),(\S+),\s*(\d+)""".r
    pattern.findFirstMatchIn(s) match {
      case Some(m) =>
        Try(
          GPSPoint(
            m.group(1).toDouble,
            m.group(2).toDouble,
            if (m.group(3).toDouble.isNaN) None else Some(m.group(3).toDouble),
            m.group(4).toLong)) match {
          case Failure(ex) =>
            logger.warn(s"Error while creating observation point from message '$s'", ex)
            None
          case Success(value) => Some(value)
        }
      case None =>
        logger.warn("Failed to extract observation point from message '{}' using regex", s)
        None
    }
  }

}

object GraphHopperManager {

  type EdgePair = (String, Double)

  private val distanceCalc = new DistancePlaneProjection

  private def calcDistance(last: GPSPoint, elem: GPSPoint): Double =
    distanceCalc.calcDist(last.lat, last.lon, elem.lat, elem.lon)

  /** Get total distance by highway.
    *
    * @param mappedEdges mapped edges
    * @return total distance by highway
    */
  private def getRouteTypesKm(mappedEdges: Seq[EdgePair]): Map[String, Double] =
    mappedEdges
      .groupBy { case (highway, _) => highway }
      .map { case (highway, distances) => (highway, distances.map { case (_, distance) => distance }.sum) }

  private def getEdges(edges: Seq[EdgeMatch]): List[Edge] =
    edges.toList.flatMap(edge => edge.getGpxExtensions.asScala.map(item => Edge(edge = edge, item = item)))

  private def getTracePoints(edges: List[Edge], encoder: CarFlagEncoderEnrich) =
    edges.map(edge => createTracePoint(edge.edge, edge.item, encoder))

  private def createTracePoint(edge: EdgeMatch, item: GPXExtension, encoder: CarFlagEncoderEnrich) =
    TracePoint(
      latitude = item.getEntry.lat,
      longitude = item.getEntry.lon,
      altitude = Some(item.getEntry.ele),
      time = item.getEntry.getTime,
      matchedLatitude = Some(item.getQueryResult.getSnappedPoint.lat),
      matchedLongitude = Some(item.getQueryResult.getSnappedPoint.lon),
      matchedAltitude = Some(item.getQueryResult.getSnappedPoint.ele),
      roadType = typeOfRoute(edge.getEdgeState, encoder),
      roadName = Some(edge.getEdgeState.getName),
      speedLimit = Some(encoder.getSpeed(edge.getEdgeState.getFlags).toInt),
      linearDistance = Some(item.getQueryResult.getQueryDistance)
    )

  private def typeOfRoute(edge: EdgeIteratorState, encoder: CarFlagEncoderEnrich): Option[String] =
    emptyToNone(encoder.getHighwayAsString(edge))

  private def emptyToNone(s: String): Option[String] = s match {
    case s if s.trim.isEmpty => None
    case s: String           => Some(s)
  }
}
