package it.agilelab.gis.domain.configuration

import com.graphhopper.matching.MapMatching
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.AlgorithmOptions
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.weighting.FastestWeighting
import com.graphhopper.util.PMap
import com.typesafe.config.Config
import it.agilelab.gis.core.encoder.CarFlagEncoderEnrich
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger, ValidationUtils }
import it.agilelab.gis.domain.graphhopper.AlgorithmType

import scala.util.{ Failure, Success, Try }

/** GraphHopperSettings holds [[it.agilelab.gis.domain.graphhopper.GraphHopperManager]] settings.
  * @param graphLocation location of the graph
  * @param elevationEnabled flag to signal whether elevation for coordination is enabled
  * @param contractionHierarchiesEnabled flag to signal whether contraction hierarchies post processing is enabled
  * @param measurementErrorSigma measurement error of GPS devices.
  */
case class GraphHopperSettings(
    graphLocation: String,
    elevationEnabled: Boolean,
    contractionHierarchiesEnabled: Boolean,
    mapMatchingAlgorithm: AlgorithmType,
    measurementErrorSigma: Int
)

/** GraphHopperConfiguration holds [[it.agilelab.gis.domain.graphhopper.GraphHopperManager]] configurations.
  * @param encoder car flag encoder.
  * @param hopperOSM graph hopper instance.
  * @param mapMatching map matching instance
  * @param settings graph hopper settings.
  */
case class GraphHopperConfiguration(
    encoder: CarFlagEncoderEnrich,
    hopperOSM: GraphHopperOSM,
    mapMatching: MapMatching,
    settings: GraphHopperSettings
)

object GraphHopperConfiguration extends Configuration with ValidationUtils with Logger {

  /** Creates a [[GraphHopperConfiguration]] instance by reading the given conf.
    * @param config configuration to read.
    * @return a [[GraphHopperConfiguration]] instance.
    */
  def apply(config: Config): GraphHopperConfiguration = {

    val parsedConfig: Try[GraphHopperConfiguration] = for {
      (encoder, hopperOSM, mapMatching, settings) <- tryOrLog(createObjects(config))
    } yield GraphHopperConfiguration(encoder, hopperOSM, mapMatching, settings)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

  private def createObjects(conf: Config): (CarFlagEncoderEnrich, GraphHopperOSM, MapMatching, GraphHopperSettings) = {
    val parsed = for {
      graphLocationRaw              <- read[String](conf, GRAPH_LOCATION.value)
      graphLocation                 <- checkIsDirectory(graphLocationRaw)
      elevationEnabled              <- read[Boolean](conf, ELEVATION_ENABLED.value)
      contractionHierarchiesEnabled <- read[Boolean](conf, CONTRACTION_HIERARCHIES_ENABLED.value)
      mapMatchingAlgorithmRaw       <- read[String](conf, MAP_MATCHING_ALGORITHM.value)
      mapMatchingAlgorithm          <- tryOrLog(AlgorithmType.fromValue(mapMatchingAlgorithmRaw))
      measurementErrorSigma         <- read[Int](conf, MEASUREMENT_ERROR_SIGMA.value)
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

        (encoder, hopperOSM, mapMatching, settings)
    }
  }
}
