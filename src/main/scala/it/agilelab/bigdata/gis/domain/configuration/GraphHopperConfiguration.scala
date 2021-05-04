package it.agilelab.bigdata.gis.domain.configuration

import com.graphhopper.matching.MapMatching
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.weighting.FastestWeighting
import com.graphhopper.util.PMap
import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.encoder.CarFlagEncoderEnrich
import it.agilelab.bigdata.gis.core.utils.{ Configuration, ConfigurationProperties, Logger, ValidationUtils }
import it.agilelab.bigdata.gis.domain.graphhopper.AlgorithmType

import scala.util.{ Failure, Success, Try }

case class GraphHopperSettings(
    graphLocation: String,
    elevationEnabled: Boolean,
    contractionHierarchiesEnabled: Boolean,
    mapMatchingAlgorithm: AlgorithmType,
    measurementErrorSigma: Int
)

case class GraphHopperConfiguration(encoder: CarFlagEncoderEnrich, hopperOSM: GraphHopperOSM, mapMatching: MapMatching)

object GraphHopperConfiguration extends Configuration with ValidationUtils with Logger {

  def apply(config: Config): GraphHopperConfiguration = {

    val parsedConfig: Try[GraphHopperConfiguration] = for {
      (encoder, hopperOSM, mapMatching) <- tryOrLog(createObjects(config))
    } yield GraphHopperConfiguration(encoder, hopperOSM, mapMatching)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

  private def createObjects(conf: Config): (CarFlagEncoderEnrich, GraphHopperOSM, MapMatching) = {
    val parsed = for {
      graphLocationRaw <- read[String](conf, ConfigurationProperties.GRAPH_LOCATION.value)
      graphLocation    <- checkIsDirectory(graphLocationRaw)
      elevationEnabled <- read[Boolean](conf, ConfigurationProperties.ELEVATION_ENABLED.value)
      contractionHierarchiesEnabled <- read[Boolean](
        conf,
        ConfigurationProperties.CONTRACTION_HIERARCHIES_ENABLED.value)
      mapMatchingAlgorithmRaw <- read[String](conf, ConfigurationProperties.MAP_MATCHING_ALGORITHM.value)
      mapMatchingAlgorithm    <- tryOrLog(AlgorithmType.fromValue(mapMatchingAlgorithmRaw))
      measurementErrorSigma   <- read[Int](conf, ConfigurationProperties.MEASUREMENT_ERROR_SIGMA.value)
    } yield GraphHopperSettings(
      graphLocation,
      elevationEnabled,
      contractionHierarchiesEnabled,
      mapMatchingAlgorithm,
      measurementErrorSigma)

    parsed match {
      case Failure(exception) => throw exception
      case Success(settings)  =>
        //Set the the elevation flag to true to include 3d dimension
        val hopperOSM = new GraphHopperOSM
        hopperOSM.setElevation(settings.elevationEnabled)
        val encoder =
          new CarFlagEncoderEnrich() // TODO refactor to use a generic trait and not a single encoder implementation
        hopperOSM.setEncodingManager(new EncodingManager(encoder))
        val weighting =
          new FastestWeighting(
            encoder,
            new PMap()
          ) // TODO refactor to use a generic trait and not a single weighting implementation

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
