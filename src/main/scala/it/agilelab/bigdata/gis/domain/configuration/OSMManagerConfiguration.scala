package it.agilelab.bigdata.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.{ Configuration, ConfigurationProperties, Logger }

import scala.util.{ Failure, Success, Try }

case class OSMManagerConfiguration(
    vehicle: String,
    filterEmptyStreets: Boolean,
    roadTolMeters: Double,
    addressTolMeters: Double,
    indexConf: Config
)

object OSMManagerConfiguration extends Configuration with Logger {

  def apply(config: Config): OSMManagerConfiguration = {

    val parsedConfig: Try[OSMManagerConfiguration] = for {

      vehicle            <- read[String](config, ConfigurationProperties.VEHICLE.value)
      filterEmptyStreets <- read[Boolean](config, ConfigurationProperties.FILTER_EMPTY_STREETS.value)
      roadTolMeters      <- read[Double](config, ConfigurationProperties.READ_TOL_METERS.value)
      addressTolMeters   <- read[Double](config, ConfigurationProperties.ADDRESS_TOL_METERS.value)
      indexConfig        <- read[Config](config, ConfigurationProperties.INDEX.value)

    } yield OSMManagerConfiguration(vehicle, filterEmptyStreets, roadTolMeters, addressTolMeters, indexConfig)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
