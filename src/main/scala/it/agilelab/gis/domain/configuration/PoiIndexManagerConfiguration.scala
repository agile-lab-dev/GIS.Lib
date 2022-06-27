package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties.{
  INPUT_PATHS,
  OSM_INDEX_OUTPUT_PATHS,
  OSM_INDEX_SERIALIZED_INPUT_FLAG,
  PATH
}
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

/** PoiIndexManagerConfiguration holds configuration for [[it.agilelab.gis.domain.managers.PoiIndexManager]]
  *
  * @param inputPaths indices input paths
  * @param isSerializedInputPaths signal whether the [[inputPaths]] references a directory with serialized indices
  * @param outputPaths output paths for serialized indices
  * @param pathConf paths configuration
  */
case class PoiIndexManagerConfiguration(
    inputPaths: List[String],
    isSerializedInputPaths: Boolean,
    outputPaths: Option[List[String]],
    pathConf: Config
)

object PoiIndexManagerConfiguration extends Configuration with Logger {

  /** Creates a [[PoiIndexManagerConfiguration]] instance by reading the given conf.
    *
    * @param config configuration to read.
    * @return a [[PoiIndexManagerConfiguration]] instance.
    */
  def apply(config: Config): PoiIndexManagerConfiguration = {

    val parsedConfig: Try[PoiIndexManagerConfiguration] = for {

      inputPaths             <- read[List[String]](config, INPUT_PATHS.value)
      outputPaths            <- readOptional[List[String]](config, OSM_INDEX_OUTPUT_PATHS.value)
      isSerializedInputPaths <- readOptional[Boolean](config, OSM_INDEX_SERIALIZED_INPUT_FLAG.value)
      pathConf               <- read[Config](config, PATH.value)

    } yield PoiIndexManagerConfiguration(inputPaths, isSerializedInputPaths.getOrElse(false), outputPaths, pathConf)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
