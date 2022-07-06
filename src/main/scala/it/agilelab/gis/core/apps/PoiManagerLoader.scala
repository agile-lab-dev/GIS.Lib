package it.agilelab.gis.core.apps

import com.typesafe.config.{ Config, ConfigFactory }
import it.agilelab.gis.core.utils.ConfigurationProperties.POI
import it.agilelab.gis.core.utils.{ Configuration, Logger }
import it.agilelab.gis.domain.managers.PoiManager

import scala.util.{ Failure, Success }

object PoiManagerLoader extends Logger with Configuration {

  def main(args: Array[String]): Unit = {
    val maybeConfig = read[Config](ConfigFactory.load(), POI.value)
    val config = maybeConfig match {
      case Success(value) =>
        value
      case Failure(exception) => throw exception
    }
    logger.info("PoiManager Config {}", config)
    PoiManager(config)
  }

}
