package it.agilelab.gis.core.apps

import com.typesafe.config.ConfigFactory
import it.agilelab.gis.core.utils.ConfigurationProperties.POI
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.managers.PoiManager

object PoiManagerLoader extends Logger {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load().getConfig(POI.value)
    logger.info("PoiManager Config {}", config)
    PoiManager(config)
  }

}
