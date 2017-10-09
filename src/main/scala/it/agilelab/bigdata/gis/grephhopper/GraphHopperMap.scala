package it.agilelab.bigdata.gis.grephhopper

import com.graphhopper.matching.{MapMatching, MatchResult}

import scalax.file.Path
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.{CarFlagEncoder, EncodingManager}
import com.graphhopper.routing.weighting.{FastestWeighting, ShortestWeighting}
import com.graphhopper.util.{GPXEntry, Parameters}

import scala.util.Try

/**
  * Created by stefano on 09/10/17.
  *
  * This object loads the graph presents in the project resources.
  * It contains the main methods of the library.
  *
  */
class GraphHopperMap(val graphLocation: String, val fileString: String, val vehicle: String, val path: String) {

  //Auxiliary constructor
  def this(graphLocation: String, vehicle: String, path: String) {
    this(graphLocation, "", vehicle, path)
  }

  //Check if the directory is empty
  val dir: Path = Path.fromString(graphLocation)
  if(!dir.isDirectory) throw new IllegalArgumentException("Expected a directory as graph's location")

  val hopperOSM = new GraphHopperOSM

  //Set the the elevation flag to true to include 3d dimension
  hopperOSM.setElevation(true)
  //We use the profile: (vehicle|path)
  hopperOSM.setEncodingManager(new EncodingManager(vehicle))
  hopperOSM.getCHFactoryDecorator.addWeighting(path)
  //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
  hopperOSM.getCHFactoryDecorator.setEnabled(false)

  if(fileString.isEmpty) {
    //If no new map is specified, load from the resource folder
    hopperOSM.load(graphLocation)
  }
  else{
    //We assume there already is a stored graph in the folder. We delete those files
    val path: Path = Path.fromString(graphLocation)
    //If false then method will throw an exception when encountering a file that cannot be deleted.  Otherwise it will continue
    //to delete all the files that can be deleted. This method is not transactional, all files visited before failure are deleted.
    Try(path.deleteRecursively(continueOnFailure = false))

    //Set the .osm file and load directly from the map
    hopperOSM.setDataReaderFile(fileString)
    hopperOSM.importOrLoad()
  }

  val algorithm: String = Parameters.Algorithms.DIJKSTRA_BI

  //TODO instantiate a FlagEncoder or a Weighting from a string
  val weighting = new ShortestWeighting(new CarFlagEncoder)
  val algoOptions: AlgorithmOptions = new AlgorithmOptions(algorithm, weighting)
  val mapMatching: MapMatching = new MapMatching(hopperOSM, algoOptions)


  def predictedRoute(gpsPoints: java.util.List[GPXEntry]): MatchedRoute = {

      val calcRoute: MatchResult = mapMatching.doWork(gpsPoints)

      MatchedRoute(calcRoute)

  }

}
