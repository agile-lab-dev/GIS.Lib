package it.agilelab.bigdata.gis.domain.graphhopper

import java.io.File
import java.util

import com.graphhopper.matching.{EdgeMatch, GPXExtension, MapMatching, MatchResult}
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.weighting.FastestWeighting
import com.graphhopper.util.details.PathDetailsBuilderFactory
import com.graphhopper.util.{GPXEntry, PMap, Parameters}
import com.typesafe.config.{Config, ConfigFactory}
import it.agilelab.bigdata.gis.core.encoder.CarFlagEncoderEnrich

import scala.collection.JavaConversions._

private case class GraphHopperEnvelope(minLat: Double,
                                       minLon: Double,
                                       maxLat: Double,
                                       maxLon: Double) {

  def containsWholeRoute(gpsPoints: List[GPXEntry]): Boolean =
    gpsPoints forall covers

  def covers(point: GPXEntry): Boolean =
    point.lat >= minLat && point.lat <= maxLat && point.lon >= minLon && point.lon <= maxLon

}

object GraphHopperManager {

  private var hopperOSM: GraphHopperOSM = _
  private var encoder: CarFlagEncoderEnrich = _
  private var weighting: FastestWeighting = _
  private var mapMatching: MapMatching = _

  private val conf: Config = ConfigFactory.load()
  private val vehicle: String = conf.getString("osm.vehicle")

  //Create the actual graph to be queried
  def init(graphLocation: String): Unit = {
    if (hopperOSM == null) {
      hopperOSM = new GraphHopperOSM

      //Check if the graphLocation is a directory
      val dir: File = new File(graphLocation)
      if (!dir.isDirectory)
        throw new IllegalArgumentException(
          "Expected a directory as graph's location"
        )

      //Set the the elevation flag to true to include 3d dimension
      hopperOSM.setElevation(true)

      //We use Generic Weighting with the DataFlagEncoder
      encoder = new CarFlagEncoderEnrich()
      hopperOSM.setEncodingManager(new EncodingManager(encoder))
      hopperOSM.setElevation(true)
      weighting = new FastestWeighting(encoder, new PMap())
      //hopperOSM.getCHFactoryDecorator.addWeighting(weighting)

      //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
      hopperOSM.getCHFactoryDecorator.setEnabled(false)

      //If no new map is specified, load from the resource folder
      hopperOSM.load(graphLocation)

      val algorithm: String = Parameters.Algorithms.DIJKSTRA_BI
      val algoOptions: AlgorithmOptions =
        new AlgorithmOptions(algorithm, weighting)
      mapMatching = new MapMatching(hopperOSM, algoOptions)
      mapMatching.setMeasurementErrorSigma(50)
      //mapMatching.setMeasurementErrorSigma(20)

    }
  }

  def graphGetter: GraphHopperOSM = {
    hopperOSM
  }

  def typeOfRoute(edge: EdgeMatch): String =
    encoder.getHighwayAsString(edge.getEdgeState) match {
      case null => "unclassified" //todo is right this
      case value: String => value
    }

  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  def matchingRoute(gpsPoints: Seq[GPSPoint]): MatchedRoute = {

    if (hopperOSM == null)
      throw new IllegalAccessException("Cannot perform map matching without a graph! Call init method first")

    val calcRoute: MatchResult = mapMatching.doWork(gpsPoints.map(_.toGPXEntry))

    val length = calcRoute.getMatchLength
    val time = calcRoute.getMatchMillis
    val edges: Seq[EdgeMatch] = calcRoute.getEdgeMatches

    if (calcRoute.getMergedPath.calcEdges().nonEmpty) {

      val mappedEdges: Seq[(String, Double)] =
        edges
          .map(edge =>
            (encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance)
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
                    Some(item.getEntry.getTime),
                    Some(item.getQueryResult.getSnappedPoint.lat),
                    Some(item.getQueryResult.getSnappedPoint.lon),
                    Some(item.getQueryResult.getSnappedPoint.ele),
                    Some(typeOfRoute(edge)),
                    Some(edge.getEdgeState.getName),
                    Some(encoder.getSpeed(item.getQueryResult.getClosestEdge.getFlags).toInt),
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

      // distances beetween node
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
                subList.last.node.get.time.get - subList.head.node.get.time.get,
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
        routeTypesKm,
        distancesBetweenNode
      )
    } else
      MatchedRoute(gpsPoints.map(_.toTracePoint), length, time, Map.empty, Seq.empty)
  }
}
