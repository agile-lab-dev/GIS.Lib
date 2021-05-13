package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

case class IndexManagerConfiguration(
    inputPaths: List[String],
    isSerializedInputPaths: Boolean,
    outputPaths: Option[List[String]],
    pathConf: Config,
    boundaryConf: Config
)

object IndexManagerConfiguration extends Configuration with Logger {

  def apply(config: Config): IndexManagerConfiguration = {

    val parsedConfig: Try[IndexManagerConfiguration] = for {

      inputPaths             <- read[List[String]](config, INPUT_PATHS.value)
      outputPaths            <- readOptional[List[String]](config, OSM_INDEX_OUTPUT_PATHS.value)
      isSerializedInputPaths <- readOptional[Boolean](config, OSM_INDEX_SERIALIZED_INPUT_FLAG.value)
      pathConf               <- read[Config](config, PATH.value)
      boundaryConf           <- read[Config](config, BOUNDARY.value)

    } yield IndexManagerConfiguration(
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
