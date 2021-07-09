package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

/** [[OSMManagerConfiguration]] holds [[it.agilelab.gis.domain.managers.OSMManager]] configurations.
  * @param vehicle vehicle type
  * @param filterEmptyStreets signal whether empty streets should be skipped.
  * @param roadTolMeters maximum distance to find a road
  * @param addressTolMeters maximum distance to find an address.
  * @param indexConf index configuration
  */
case class OSMManagerConfiguration(
    vehicle: String,
    filterEmptyStreets: Boolean,
    roadTolMeters: Double,
    addressTolMeters: Double,
    indexConf: Config
)

object OSMManagerConfiguration extends Configuration with Logger {

  /** Creates a [[OSMManagerConfiguration]] instance by reading the given conf.
    * @param config configuration to read.
    * @return a [[OSMManagerConfiguration]] instance.
    */
  def apply(config: Config): OSMManagerConfiguration = {

    val parsedConfig: Try[OSMManagerConfiguration] = for {

      vehicle            <- read[String](config, VEHICLE.value)
      filterEmptyStreets <- read[Boolean](config, FILTER_EMPTY_STREETS.value)
      roadTolMeters      <- read[Double](config, READ_TOL_METERS.value)
      addressTolMeters   <- read[Double](config, ADDRESS_TOL_METERS.value)
      indexConfig        <- read[Config](config, INDEX.value)

    } yield OSMManagerConfiguration(vehicle, filterEmptyStreets, roadTolMeters, addressTolMeters, indexConfig)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
