package it.agilelab.gis.core.apps

import com.typesafe.config.ConfigFactory
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.managers.OSMManager

object OSMManagerLoader extends Logger {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    logger.info("Config {}", config)
    OSMManager(config.getConfig("osm"))
  }
}
