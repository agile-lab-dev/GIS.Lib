package it.agilelab.gis.core.apps

import com.typesafe.config.ConfigFactory
import it.agilelab.gis.core.utils.ConfigurationProperties.GEOCODE
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.managers.GeocodeManager

object GeocodeManagerLoader extends Logger {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load().getConfig(GEOCODE.value)
    logger.info("Geocode Config {}", config)
    GeocodeManager(config)
  }
}
