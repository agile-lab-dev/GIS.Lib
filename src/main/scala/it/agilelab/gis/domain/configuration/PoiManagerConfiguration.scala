package it.agilelab.gis.domain.configuration

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.ConfigurationProperties._
import it.agilelab.gis.core.utils.{ Configuration, Logger }

import scala.util.{ Failure, Success, Try }

/** [[PoiManagerConfiguration]] holds [[it.agilelab.gis.domain.managers.PoiManager]] configurations
  *
  * @param filterEmptyAmenity signal whether empty amenity results should be skipped.
  * @param filterEmptyLanduse signal whether empty landuse results should be skipped.
  * @param filterEmptyLeisure signal whether empty leisure results should be skipped.
  * @param filterEmptyNatural signal whether empty natural results should be skipped.
  * @param filterEmptyShop signal whether empty shop results should be skipped.
  * @param indexConf index configuration
  */
case class PoiManagerConfiguration(
    filterEmptyAmenity: Boolean,
    filterEmptyLanduse: Boolean,
    filterEmptyLeisure: Boolean,
    filterEmptyNatural: Boolean,
    filterEmptyShop: Boolean,
    indexConf: Config
)

object PoiManagerConfiguration extends Configuration with Logger {

  /** Creates a [[PoiManagerConfiguration]] instance by reading the given conf.
    *
    * @param config configuration to read.
    * @return a [[PoiManagerConfiguration]] instance.
    */
  def apply(config: Config): PoiManagerConfiguration = {

    val parsedConfig: Try[PoiManagerConfiguration] = for {
      filterEmptyAmenity <- read[Boolean](config, FILTER_EMPTY_POI_AMENITY.value)
      filterEmptyLanduse <- read[Boolean](config, FILTER_EMPTY_POI_LANDUSE.value)
      filterEmptyLeisure <- read[Boolean](config, FILTER_EMPTY_POI_LEISURE.value)
      filterEmptyNatural <- read[Boolean](config, FILTER_EMPTY_POI_NATURAL.value)
      filterEmptyShop    <- read[Boolean](config, FILTER_EMPTY_POI_SHOP.value)
      indexConfig        <- read[Config](config, INDEX.value)

    } yield PoiManagerConfiguration(
      filterEmptyAmenity,
      filterEmptyLanduse,
      filterEmptyLeisure,
      filterEmptyNatural,
      filterEmptyShop,
      indexConfig)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }

}
