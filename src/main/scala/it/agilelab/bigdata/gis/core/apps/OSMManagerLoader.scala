package it.agilelab.bigdata.gis.core.apps

import com.typesafe.config.ConfigFactory
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.managers.OSMManager

object OSMManagerLoader extends Logger {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    logger.info("Config {}", config)
    OSMManager(config.getConfig("osm"))
  }
}
