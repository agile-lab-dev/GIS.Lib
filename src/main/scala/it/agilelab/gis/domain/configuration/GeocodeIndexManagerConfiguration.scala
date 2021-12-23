package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

/** IndexManagerConfiguration holds configuration for [[it.agilelab.gis.domain.managers.GeocodeIndexManager]]
  *
  * @param inputPaths indices input paths
  * @param isSerializedInputPaths signal whether the [[inputPaths]] references a directory with serialized indices
  * @param outputPaths output paths for serialized indices
  * @param pathConf paths configuration
  * @param boundaryConf boundaries configuration
  */
case class GeocodeIndexManagerConfiguration(
    inputPaths: List[String],
    isSerializedInputPaths: Boolean,
    outputPaths: Option[List[String]],
    pathConf: Config,
    boundaryConf: Config
)

object GeocodeIndexManagerConfiguration extends Configuration with Logger {

  /** Creates a [[GeocodeIndexManagerConfiguration]] instance by reading the given conf.
    *
    * @param config configuration to read.
    * @return a [[GeocodeIndexManagerConfiguration]] instance.
    */
  def apply(config: Config): GeocodeIndexManagerConfiguration = {

    val parsedConfig: Try[GeocodeIndexManagerConfiguration] = for {

      inputPaths             <- read[List[String]](config, INPUT_PATHS.value)
      outputPaths            <- readOptional[List[String]](config, OSM_INDEX_OUTPUT_PATHS.value)
      isSerializedInputPaths <- readOptional[Boolean](config, OSM_INDEX_SERIALIZED_INPUT_FLAG.value)
      pathConf               <- read[Config](config, PATH.value)
      boundaryConf           <- read[Config](config, BOUNDARY.value)

    } yield GeocodeIndexManagerConfiguration(
      inputPaths,
      isSerializedInputPaths.getOrElse(false),
      outputPaths,
      pathConf,
      boundaryConf)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
