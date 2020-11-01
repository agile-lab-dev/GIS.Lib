package it.agilelab.bigdata.gis.domain.graphhopper

import java.util.Locale

import com.graphhopper.matching.{EdgeMatch, MapMatching, MatchResult}
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.{DataFlagEncoder, EncodingManager}
import com.graphhopper.routing.weighting.{FastestWeighting, GenericWeighting}
import com.graphhopper.util.shapes.{GHPoint, GHPoint3D}
import com.graphhopper.util.{GPXEntry, PMap, Parameters}
import com.graphhopper.{GHRequest, GHResponse, PathWrapper}
import com.typesafe.config.{Config, ConfigFactory}
import it.agilelab.bigdata.gis.core.encoder.CarFlagEncoderEnrich

import scala.collection.JavaConversions._
import scalax.file.Path

/**
  * @author Stefano Samele
  *
  * This object loads the graph presents in the project resources.
  * It contains two methods: one for map-matching (matchingRoute) and
  * the other one for extend a sequence of points through routing (extendRoute)
  *
  */

private case class GraphHopperEnvelope(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double) {

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
  def init(graphLocation: String): AnyVal = {
    if (hopperOSM == null) {
      hopperOSM = new GraphHopperOSM

      //Check if the graphLocation is a directory
      val dir: Path = Path.fromString(graphLocation)
      if (!dir.isDirectory) throw new IllegalArgumentException("Expected a directory as graph's location")

      //Set the the elevation flag to true to include 3d dimension
      hopperOSM.setElevation(true)


      //We use Generic Weighting with the DataFlagEncoder
      encoder = new CarFlagEncoderEnrich
      hopperOSM.setEncodingManager(new EncodingManager(encoder))
      weighting = new FastestWeighting(encoder, new PMap())
      hopperOSM.getCHFactoryDecorator.addWeighting(weighting)

      //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
      hopperOSM.getCHFactoryDecorator.setEnabled(false)

      //If no new map is specified, load from the resource folder
      hopperOSM.load(graphLocation)
    }
  }

  def graphGetter: GraphHopperOSM ={
    hopperOSM
  }


  //map matching method is prone to runtime exception not easily managed
  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  def matchingRoute(gpsPoints: java.util.List[GPXEntry]): MatchedRoute = {

    if (hopperOSM == null) throw new IllegalAccessException("Cannot perform map matching without a graph! Call init method first")

    val algorithm: String = Parameters.Algorithms.DIJKSTRA_BI
    val algoOptions: AlgorithmOptions = new AlgorithmOptions(algorithm, weighting)
    val mapMatching: MapMatching = new MapMatching(hopperOSM, algoOptions)
    mapMatching.setMeasurementErrorSigma(50)
    //mapMatching.setMeasurementErrorSigma(20)

    val calcRoute: MatchResult = mapMatching.doWork(gpsPoints)

    val length = calcRoute.getMatchLength
    val time = calcRoute.getMatchMillis

    val edges: Seq[EdgeMatch] = calcRoute.getEdgeMatches

    val mappedEdges: Seq[(String, Double)] =
      edges
        .map(edge => (encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance))
        .map{
          case (null,value) => ("unclassified",value)
          case x:(String, Double) => x
        }

    val routeTypesKm: Map[String, Double] =
      mappedEdges
        .groupBy(_._1)
        .map(x => (x._1, x._2.map(_._2).sum))

    val snappedPoints: Seq[(GHPoint3D, Long)] =
      edges
        .flatMap(_.getGpxExtensions)
        .map(x => (x.getQueryResult.getSnappedPoint, x.getEntry.getTime))

    val points: Seq[GPXEntry] = snappedPoints.map(x =>  new GPXEntry(x._1.lat, x._1.lon, x._1.ele, x._2))

    MatchedRoute(points, length, time, routeTypesKm)

  }

  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  def extendRoute(gpsPoints: Seq[GPXEntry]): Seq[GPXEntry] = {

    if (hopperOSM == null) throw new IllegalAccessException("Cannot calculate route extension without a graph! Call init method first")

    val res =
      gpsPoints.map(x => (new GHPoint(x.lat, x.lon), x.getTime))
        .sliding(2)
        .flatMap( x => {

          val req = new GHRequest(x.map(_._1))
            .setVehicle(vehicle)
            .setLocale(Locale.ITALY)

          val rsp: GHResponse = hopperOSM.route(req)

          //First check for errors
          if(rsp.hasErrors){
            // handle them!
            val errors = rsp.getErrors.toSeq
            val message = errors.map( err =>
              s"Error #${errors.indexOf(err)}: ${err.getMessage}"
            ).mkString("\n")
            throw new RuntimeException(message)
          }

          // use the best path, see the GHResponse class for more possibilities.
          val path: PathWrapper = rsp.getBest

          val numAddedPoints: Int = path.getPoints.getSize

          val finalTime: Long = x.seq.map(_._2).reverse.head

          val initialTime: Long = x.seq.map(_._2).head

          val timeTaken: Long = finalTime - initialTime

          val steps: Seq[Long] = (0 until numAddedPoints-1).map(x => initialTime + x*timeTaken/numAddedPoints)

          path.getPoints.take(numAddedPoints-1).zip(steps)
            .map( x => new GPXEntry( x._1.lat, x._1.lon, x._2))

        }).toSeq :+ gpsPoints.reverse.head

    res

  }



}