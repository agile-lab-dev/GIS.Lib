package it.agilelab.bigdata.gis.domain.loader

import com.graphhopper.matching.MapMatching
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.weighting.FastestWeighting
import com.graphhopper.util.PMap
import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.encoder.CarFlagEncoderEnrich
import it.agilelab.bigdata.gis.core.utils.{Configuration, Logger, ValidationUtils}
import it.agilelab.bigdata.gis.domain.graphhopper.AlgorithmType

import scala.util.{Failure, Success, Try}

case class GraphHopperSettings(graphLocation: String,
                               elevationEnabled: Boolean,
                               contractionHierarchiesEnabled: Boolean,
                               mapMatchingAlgorithm: AlgorithmType,
                               measurementErrorSigma: Int)

case class GraphHopperConfiguration(encoder: CarFlagEncoderEnrich,
                                    hopperOSM: GraphHopperOSM,
                                    mapMatching: MapMatching)

object GraphHopperConfiguration extends Configuration with ValidationUtils with Logger {

  private val OSM: String = "osm"
  private val GRAPH_LOCATION: String = "graph_location"
  private val ELEVATION_ENABLED: String = "elevation_enabled"
  private val MAP_MATCHING_ALGORITHM: String = "map_matching_algorithm"
  private val MEASUREMENT_ERROR_SIGMA: String = "measurement_error_sigma"
  private val CONTRACTION_HIERARCHIES_ENABLED: String = "contraction_hierarchies_enabled"

  def apply(config: Config): GraphHopperConfiguration = {

     val parsedConfig :Try[GraphHopperConfiguration] = for {
      osm <- read[Config](config, OSM)
      (encoder, hopperOSM, mapMatching)  <- tryOrLog(createObjects(osm))
    } yield GraphHopperConfiguration(encoder, hopperOSM, mapMatching)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

  private def createObjects(conf: Config): (CarFlagEncoderEnrich, GraphHopperOSM, MapMatching ) =  {
    val parsed = for {
      graphLocationRaw <- read[String](conf, GRAPH_LOCATION)
      graphLocation <- checkIsDirectory(graphLocationRaw)
      elevationEnabled <-  read[Boolean](conf, ELEVATION_ENABLED)
      contractionHierarchiesEnabled <- read[Boolean](conf, CONTRACTION_HIERARCHIES_ENABLED)
      mapMatchingAlgorithmRaw <- read[String](conf, MAP_MATCHING_ALGORITHM)
      mapMatchingAlgorithm <- tryOrLog(AlgorithmType.fromValue(mapMatchingAlgorithmRaw))
      measurementErrorSigma <- read[Int](conf, MEASUREMENT_ERROR_SIGMA)
    } yield GraphHopperSettings(graphLocation, elevationEnabled, contractionHierarchiesEnabled, mapMatchingAlgorithm, measurementErrorSigma)

    parsed match {
      case Failure(exception) => throw exception
      case Success(settings) => {
        //Set the the elevation flag to true to include 3d dimension
        val hopperOSM = new GraphHopperOSM
        hopperOSM.setElevation(settings.elevationEnabled)
        val encoder = new CarFlagEncoderEnrich()
        hopperOSM.setEncodingManager(EncodingManager.create(encoder))
        val weighting = new FastestWeighting(encoder, new PMap())

        //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
        hopperOSM.getCHFactoryDecorator.setEnabled(settings.contractionHierarchiesEnabled)

        //If no new map is specified, load from the resource folder
        hopperOSM.load(settings.graphLocation)
        val algorithm: String = settings.mapMatchingAlgorithm.value
        val algoOptions: AlgorithmOptions = new AlgorithmOptions(algorithm, weighting)
        val mapMatching = new MapMatching(hopperOSM, algoOptions)
        mapMatching.setMeasurementErrorSigma(settings.measurementErrorSigma)

        (encoder, hopperOSM, mapMatching)
      }
   }
  }
}