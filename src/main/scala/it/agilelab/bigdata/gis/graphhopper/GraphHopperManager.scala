package it.agilelab.bigdata.gis.graphhopper

import java.util.Locale

import com.graphhopper.{GHRequest, GHResponse, PathWrapper}
import com.graphhopper.matching.{EdgeMatch, MapMatching, MatchResult}


import scalax.file.Path
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.{DataFlagEncoder, EncodingManager}
import com.graphhopper.routing.weighting.GenericWeighting
import com.graphhopper.util.shapes.GHPoint
import com.graphhopper.util.{GPXEntry, PMap, Parameters}

import scala.collection.JavaConversions._

/**
  * @author Stefano Samele
  *
  * This object loads the graph presents in the project resources.
  * It contains two methods: one for map-matching (matchingRoute) and
  * the other one for extend a sequence of points through routing (extendRoute)
  *
  */
object GraphHopperManager {

  private var hopperOSM: GraphHopperOSM = _
  private var encoder: DataFlagEncoder = _
  private var weighting: GenericWeighting = _

  //Create the actual graph to be queried
  def init(graphLocation: String) = {
    if (hopperOSM == null) {
      hopperOSM = new GraphHopperOSM

      //Check if the graphLocation is a directory
      val dir: Path = Path.fromString(graphLocation)
      if (!dir.isDirectory) throw new IllegalArgumentException("Expected a directory as graph's location")

      //Set the the elevation flag to true to include 3d dimension
      hopperOSM.setElevation(true)


      //We use Generic Weighting with the DataFlagEncoder
      encoder = new DataFlagEncoder
      hopperOSM.setEncodingManager(new EncodingManager(encoder))
      weighting = new GenericWeighting(encoder, new PMap())
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

    val mappedEdges: Seq[(String, Double)] = edges.map(edge => (encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance))

    val routeTypesKm = mappedEdges.groupBy(_._1).map(x => (x._1, x._2.map(_._2).sum))

    val snappedPoints = edges.flatMap(_.getGpxExtensions).map(x => (x.getQueryResult.getSnappedPoint, x.getEntry.getTime))

    val points = snappedPoints.map(x =>  new GPXEntry(x._1.lat, x._1.lon, x._1.ele, x._2))

    MatchedRoute(points, length, time, routeTypesKm)

  }

  @throws(classOf[RuntimeException])
  @throws(classOf[IllegalAccessException])
  def extendRoute(gpsPoints: Seq[GPXEntry]): Seq[GPXEntry] = {

   if (hopperOSM == null) throw new IllegalAccessException("Cannot calculate route extension without a graph! Call init method first")

   gpsPoints.map(x => (new GHPoint(x.lat, x.lon), x.getTime))
      .sliding(2)
      .map( x => {

        val req = new GHRequest(x.map(_._1))
          .setVehicle("generic")
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

      }).flatten.toSeq :+ gpsPoints.reverse.head

  }



}