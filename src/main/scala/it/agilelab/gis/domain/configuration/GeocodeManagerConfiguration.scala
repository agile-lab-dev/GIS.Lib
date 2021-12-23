package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

/** [[GeocodeManagerConfiguration]] holds [[it.agilelab.gis.domain.managers.GeocodeManager]] and
  * [[it.agilelab.gis.domain.managers.GeoRelationManager]] configurations.
  *
  * @param vehicle vehicle type
  * @param filterEmptyStreets signal whether empty streets should be skipped.
  * @param roadTolMeters maximum distance to find a road
  * @param addressTolMeters maximum distance to find an address.
  * @param indexConf index configuration
  */
case class GeocodeManagerConfiguration(
    vehicle: String,
    filterEmptyStreets: Boolean,
    roadTolMeters: Double,
    addressTolMeters: Double,
    indexConf: Config
)

object GeocodeManagerConfiguration extends Configuration with Logger {

  /** Creates a [[GeocodeManagerConfiguration]] instance by reading the given conf.
    *
    * @param config configuration to read.
    * @return a [[GeocodeManagerConfiguration]] instance.
    */
  def apply(config: Config): GeocodeManagerConfiguration = {

    val parsedConfig: Try[GeocodeManagerConfiguration] = for {

      vehicle            <- read[String](config, VEHICLE.value)
      filterEmptyStreets <- read[Boolean](config, FILTER_EMPTY_STREETS.value)
      roadTolMeters      <- read[Double](config, READ_TOL_METERS.value)
      addressTolMeters   <- read[Double](config, ADDRESS_TOL_METERS.value)
      indexConfig        <- read[Config](config, INDEX.value)

    } yield GeocodeManagerConfiguration(vehicle, filterEmptyStreets, roadTolMeters, addressTolMeters, indexConfig)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
