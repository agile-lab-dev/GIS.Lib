package it.agilelab.bigdata.gis.domain.graphhopper

import java.io.File
import java.util.Locale

import com.graphhopper.matching.{EdgeMatch, MapMatching, MatchResult}
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.weighting.FastestWeighting
import com.graphhopper.util.details.PathDetailsBuilderFactory
import com.graphhopper.util.shapes.{GHPoint, GHPoint3D}
import com.graphhopper.util.{GPXEntry, PMap, Parameters}
import com.graphhopper.{GHRequest, GHResponse, PathWrapper}
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
      weighting = new FastestWeighting(encoder, new PMap())
      hopperOSM.getCHFactoryDecorator.addWeighting(weighting)

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

  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  def matchingRoute(gpsPoints: java.util.List[GPXEntry]): MatchedRoute = {

    //println("POINTS: " + gpsPoints)

    if (hopperOSM == null)
      throw new IllegalAccessException(
        "Cannot perform map matching without a graph! Call init " +
          "method first"
      )
    val calcRoute: MatchResult = mapMatching.doWork(gpsPoints)

    val length = calcRoute.getMatchLength
    val time = calcRoute.getMatchMillis
    val edges: Seq[EdgeMatch] = calcRoute.getEdgeMatches

    if (calcRoute.getMergedPath.calcEdges().nonEmpty) {

      val mappedEdges: Seq[(String, Double)] =
        edges
          .map(
            edge =>
              (
                encoder.getHighwayAsString(edge.getEdgeState),
                edge.getEdgeState.getDistance
            )
          )
          .map {
            case (null, value)       => ("unclassified", value)
            case x: (String, Double) => x
          }

      //retrieve distance for each pair of points
      val distances = calcRoute.getMergedPath
        .calcDetails(List("distance"), new PathDetailsBuilderFactory, 0)
        .get("distance")

      //retrieve all edges
      val alledges: Seq[EnrichEdge] = edges.toList.flatMap(edge => {
        val gpsExtensions = edge.getGpxExtensions
        val edgeId = edge.getEdgeState.getBaseNode

        if (gpsExtensions.size() == 0)
          List(EnrichEdge(edgeId, isInitialNode = false, None))
        else
          gpsExtensions.flatMap(
            item =>
              List(
                EnrichEdge(
                  item.getQueryResult.getClosestNode,
                  isInitialNode = true,
                  Some(
                    Point(
                      item.getQueryResult.getSnappedPoint.lat,
                      item.getQueryResult.getSnappedPoint.lon,
                      Some(item.getEntry.getTime)
                    )
                  )
                )
            )
          )
      })

      //union information on edge with information on distances
      val distancesBetweenEdge: Seq[EnrichEdgeWithDistance] =
        alledges.zip(distances).map {
          case (edge, distance) =>
            EnrichEdgeWithDistance(
              edge.idNode,
              edge.isInitialNode,
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

      // distances beetween initial node
      val distancesBetweenInitialNode =
        idxInitialNode
          .zip(idxInitialNode.tail)
          .map {
            case (fromIdx, toIdx) =>
              val l = distancesBetweenEdge.subList(fromIdx, toIdx + 1)
              val distance = l.foldLeft(0D)((acc, elem) => {
                acc + elem.distance
              })

              DistancePoint(
                l.head.node.get,
                l.last.node.get,
                distance,
                l.last.node.get.time.get - l.head.node.get.time.get
              )
          }

      val routeTypesKm: Map[String, Double] =
        mappedEdges
          .groupBy(_._1)
          .map(x => (x._1, x._2.map(_._2).sum))

      val snappedPoints: Seq[(GHPoint3D, Long)] =
        edges
          .flatMap(_.getGpxExtensions)
          .map(x => (x.getQueryResult.getSnappedPoint, x.getEntry.getTime))

      val points: Seq[GPXEntry] =
        snappedPoints.map(x => new GPXEntry(x._1.lat, x._1.lon, x._1.ele, x._2))

      MatchedRoute(
        points,
        length,
        time,
        routeTypesKm,
        distancesBetweenInitialNode
      )
    } else
      MatchedRoute(gpsPoints, length, time, Map.empty, Seq.empty)
  }

  /** Extend Route **/
  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  def extendRoute(gpsPoints: Seq[GPXEntry]): Seq[GPXEntry] = {

    if (hopperOSM == null)
      throw new IllegalAccessException(
        "Cannot calculate route extension without a graph! Call init method first"
      )

    val res =
      gpsPoints
        .map(x => (new GHPoint(x.lat, x.lon), x.getTime))
        .sliding(2)
        .flatMap(x => {

          val req = new GHRequest(x.map(_._1))
            .setVehicle(vehicle)
            .setLocale(Locale.ITALY)

          val rsp: GHResponse = hopperOSM.route(req)

          //First check for errors
          if (rsp.hasErrors) {
            // handle them!
            val errors = rsp.getErrors.toSeq
            val message = errors
              .map(err => s"Error #${errors.indexOf(err)}: ${err.getMessage}")
              .mkString("\n")
            throw new RuntimeException(message)
          }

          // use the best path, see the GHResponse class for more possibilities.
          val path: PathWrapper = rsp.getBest

          val numAddedPoints: Int = path.getPoints.getSize

          val finalTime: Long = x.seq.map(_._2).reverse.head

          val initialTime: Long = x.seq.map(_._2).head

          val timeTaken: Long = finalTime - initialTime

          val steps: Seq[Long] = (0 until numAddedPoints - 1)
            .map(x => initialTime + x * timeTaken / numAddedPoints)

          path.getPoints
            .take(numAddedPoints - 1)
            .zip(steps)
            .map(x => new GPXEntry(x._1.lat, x._1.lon, x._2))

        })
        .toSeq :+ gpsPoints.reverse.head

    res
  }
}
