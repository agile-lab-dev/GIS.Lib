package it.agilelab.bigdata.gis.core.utils

trait ConfigurationProperties {
  val value: String
}

object ConfigurationProperties {

  case object OSM extends ConfigurationProperties { override lazy val value: String = "osm" }
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

}
