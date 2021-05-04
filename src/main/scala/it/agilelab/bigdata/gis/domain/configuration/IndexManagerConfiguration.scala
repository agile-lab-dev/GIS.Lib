package it.agilelab.bigdata.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.{ Configuration, ConfigurationProperties, Logger }

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

      inputPaths  <- read[List[String]](config, ConfigurationProperties.INPUT_PATHS.value)
      outputPaths <- readOptional[List[String]](config, ConfigurationProperties.OSM_INDEX_OUTPUT_PATHS.value)
      isSerializedInputPaths <- readOptional[Boolean](
        config,
        ConfigurationProperties.OSM_INDEX_SERIALIZED_INPUT_FLAG.value)
      pathConf     <- read[Config](config, ConfigurationProperties.PATH.value)
      boundaryConf <- read[Config](config, ConfigurationProperties.BOUNDARY.value)

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
