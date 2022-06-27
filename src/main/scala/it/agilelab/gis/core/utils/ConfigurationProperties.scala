package it.agilelab.gis.core.utils

trait ConfigurationProperties {
  val value: String
}

object ConfigurationProperties {

  case object GEOCODE extends ConfigurationProperties { override lazy val value: String = "geocode" }
  case object GEORELATION extends ConfigurationProperties { override lazy val value: String = "georelation" }
  case object GRAPH extends ConfigurationProperties { override lazy val value: String = "graph" }
  case object INDEX extends ConfigurationProperties { override lazy val value: String = "index" }
  case object PATH extends ConfigurationProperties { override lazy val value: String = "path" }
  case object BOUNDARY extends ConfigurationProperties { override lazy val value: String = "boundary" }
  case object OSM_INDEX_SERIALIZED_INPUT_FLAG extends ConfigurationProperties {
    override lazy val value: String = "is_serialized_input_paths"
  }
  case object INPUT_PATHS extends ConfigurationProperties { override lazy val value: String = "input_paths" }
  case object OSM_INDEX_OUTPUT_PATHS extends ConfigurationProperties {
    override lazy val value: String = "serialized_output_paths"
  }
  case object VEHICLE extends ConfigurationProperties { override lazy val value: String = "vehicle" }
  case object FILTER_EMPTY_STREETS extends ConfigurationProperties {
    override lazy val value: String = "filter_empty_streets"
  }
  case object READ_TOL_METERS extends ConfigurationProperties { override lazy val value: String = "road_tol_meters" }
  case object ADDRESS_TOL_METERS extends ConfigurationProperties {
    override lazy val value: String = "address_tol_meters"
  }
  case object GRAPH_LOCATION extends ConfigurationProperties { override lazy val value: String = "graph_location" }
  case object ELEVATION_ENABLED extends ConfigurationProperties {
    override lazy val value: String = "elevation_enabled"
  }
  case object MAP_MATCHING_ALGORITHM extends ConfigurationProperties {
    override lazy val value: String = "map_matching_algorithm"
  }
  case object MEASUREMENT_ERROR_SIGMA extends ConfigurationProperties {
    override lazy val value: String = "measurement_error_sigma"
  }
  case object CONTRACTION_HIERARCHIES_ENABLED extends ConfigurationProperties {
    override lazy val value: String = "contraction_hierarchies_enabled"
  }
  case object SEA_INPUT_PATH extends ConfigurationProperties { override lazy val value: String = "sea_input_path" }

  case object POI extends ConfigurationProperties { override lazy val value: String = "poi" }
  case object FILTER_EMPTY_POI_AMENITY extends ConfigurationProperties {
    override lazy val value: String = "filter_empty_amenity"
  }
  case object FILTER_EMPTY_POI_LANDUSE extends ConfigurationProperties {
    override lazy val value: String = "filter_empty_landuse"
  }
  case object FILTER_EMPTY_POI_LEISURE extends ConfigurationProperties {
    override lazy val value: String = "filter_empty_leisure"
  }
  case object FILTER_EMPTY_POI_NATURAL extends ConfigurationProperties {
    override lazy val value: String = "filter_empty_natural"
  }
  case object FILTER_EMPTY_POI_SHOP extends ConfigurationProperties {
    override lazy val value: String = "filter_empty_shop"
  }
}
