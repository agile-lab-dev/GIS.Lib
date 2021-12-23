package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

/** IndexManagerConfiguration holds configuration for [[it.agilelab.gis.domain.managers.GeoRelationManager]]
  * @param inputPaths indices input paths
  * @param isSerializedInputPaths signal whether the [[inputPaths]] references a directory with serialized indices
  * @param outputPaths output paths for serialized indices
  * @param seaInputPath sea objects input path
  */
case class GeoRelationIndexManagerConfiguration(
    inputPaths: List[String],
    isSerializedInputPaths: Boolean,
    outputPaths: Option[List[String]],
    seaInputPath: Option[String]
)

object GeoRelationIndexManagerConfiguration extends Configuration with Logger {

  /** Creates a [[GeoRelationIndexManagerConfiguration]] instance by reading the given conf.
    *
    * @param config configuration to read.
    * @return a [[GeoRelationIndexManagerConfiguration]] instance.
    */
  def apply(config: Config): GeoRelationIndexManagerConfiguration = {

    val parsedConfig: Try[GeoRelationIndexManagerConfiguration] = for {

      inputPaths             <- read[List[String]](config, INPUT_PATHS.value)
      outputPaths            <- readOptional[List[String]](config, OSM_INDEX_OUTPUT_PATHS.value)
      isSerializedInputPaths <- readOptional[Boolean](config, OSM_INDEX_SERIALIZED_INPUT_FLAG.value)
      seaInputPath           <- readOptional[String](config, SEA_INPUT_PATH.value)

    } yield GeoRelationIndexManagerConfiguration(
      inputPaths,
      isSerializedInputPaths.getOrElse(false),
      outputPaths,
      seaInputPath)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
