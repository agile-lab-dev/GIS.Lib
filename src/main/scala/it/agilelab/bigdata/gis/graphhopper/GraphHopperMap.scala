package it.agilelab.bigdata.gis.graphhopper

import com.graphhopper.matching.{EdgeMatch, MapMatching, MatchResult}

import scalax.file.Path
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.{CarFlagEncoder, DataFlagEncoder, EncodingManager}
import com.graphhopper.routing.weighting.{FastestWeighting, GenericWeighting, ShortestWeighting}
import com.graphhopper.util.{GPXEntry, PMap, Parameters}

import scala.collection.JavaConversions._
import scala.collection.immutable
import scala.util.Try

/**
  * Created by stefano on 09/10/17.
  *
  * This object loads the graph presents in the project resources.
  * It contains the main methods of the library.
  *
  */
class GraphHopperMap(val graphLocation: String, val fileString: String) {

  //Auxiliary constructor
  def this(graphLocation: String) {
    this(graphLocation, "")
  }

  //Check if the graphLocation is a directory
  val dir: Path = Path.fromString(graphLocation)
  if(!dir.isDirectory) throw new IllegalArgumentException("Expected a directory as graph's location")

  //We create the graph object
  val hopperOSM = new GraphHopperOSM

  //Set the the elevation flag to true to include 3d dimension
  hopperOSM.setElevation(true)
  //We use Generic Weighting with the DataFlagEncoder
  val encoder = new DataFlagEncoder
  hopperOSM.setEncodingManager(new EncodingManager(encoder))
  val weighting = new GenericWeighting(encoder, new PMap())
  hopperOSM.getCHFactoryDecorator.addWeighting(weighting)
  //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
  hopperOSM.getCHFactoryDecorator.setEnabled(false)

  if(fileString.isEmpty) {
    //If no new map is specified, load from the resource folder
    hopperOSM.load(graphLocation)
  }
  else{

    //We assume that there already is a stored graph in the folder. We delete those files
    val path: Path = Path.fromString(graphLocation)
    //If false then method will throw an exception when encountering a file that cannot be deleted.  Otherwise it will continue
    //to delete all the files that can be deleted. This method is not transactional, all files visited before failure are deleted.
    Try(path.deleteRecursively(continueOnFailure = false))

    hopperOSM.setGraphHopperLocation(graphLocation)
    //Set the .osm file and load directly from the map
    hopperOSM.setDataReaderFile(fileString)
    hopperOSM.importOrLoad()
  }

  val algorithm: String = Parameters.Algorithms.DIJKSTRA_BI
  val algoOptions: AlgorithmOptions = new AlgorithmOptions(algorithm, weighting)
  println("MAX_VISITED_NODES: " + algoOptions.getMaxVisitedNodes)
  val mapMatching: MapMatching = new MapMatching(hopperOSM, algoOptions)
  mapMatching.setMeasurementErrorSigma(50)


  def matchingRoute(gpsPoints: java.util.List[GPXEntry]): MatchedRoute = {

    val calcRoute: MatchResult = mapMatching.doWork(gpsPoints)

    val length = calcRoute.getMatchLength
    val time = calcRoute.getMatchMillis

    val edges: Seq[EdgeMatch] = calcRoute.getEdgeMatches

    val mappedEdges: Seq[(String, Double)] = edges.map(edge => (encoder.getHighwayAsString(edge.getEdgeState), edge.getEdgeState.getDistance))

    val routeTypesKm = mappedEdges.groupBy(_._1).map(x => (x._1, x._2.map(_._2).sum))

    MatchedRoute(length, time, routeTypesKm)

  }



}

