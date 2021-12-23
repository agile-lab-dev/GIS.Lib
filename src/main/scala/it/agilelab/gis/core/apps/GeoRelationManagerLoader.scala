package it.agilelab.gis.core.apps

import com.typesafe.config.ConfigFactory
import it.agilelab.gis.core.utils.ConfigurationProperties.GEORELATION
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.managers.GeoRelationManager

object GeoRelationManagerLoader extends Logger {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load().getConfig(GEORELATION.value)
    logger.info("GeoRelation Config {}", config)
    GeoRelationManager(config)
  }
}
