package it.agilelab.gis.domain.models

trait OSMRailwayType {
  def value: String
}

object OSMRailwayType {

  /** Possible values of railway type */
  val values = Seq(
    ABANDONED,
    CONSTRUCTION,
    FUNICULAR,
    LIGHT_RAIL,
    MINIATURE,
    MONORAIL,
    NARROW_GAUGE,
    PRESERVED,
    RAIL,
    SUBWAY,
    TRAM,
    ND
  )

  /** Parses from input string */
  def fromValue(v: String): OSMRailwayType =
    v.toLowerCase.trim match {
      case ABANDONED.value    => ABANDONED
      case CONSTRUCTION.value => CONSTRUCTION
      case FUNICULAR.value    => FUNICULAR
      case LIGHT_RAIL.value   => LIGHT_RAIL
      case MINIATURE.value    => MINIATURE
      case MONORAIL.value     => MONORAIL
      case NARROW_GAUGE.value => NARROW_GAUGE
      case PRESERVED.value    => PRESERVED
      case RAIL.value         => RAIL
      case SUBWAY.value       => SUBWAY
      case TRAM.value         => TRAM
      case _                  => OSMRailwayType.ND
    }

  case object ABANDONED extends OSMRailwayType { lazy val value = "abandoned" }

  case object CONSTRUCTION extends OSMRailwayType { lazy val value = "construction" }

  case object FUNICULAR extends OSMRailwayType { lazy val value = "funicular" }

  case object LIGHT_RAIL extends OSMRailwayType { lazy val value = "light_rail" }

  case object MINIATURE extends OSMRailwayType { lazy val value = "miniature" }

  case object MONORAIL extends OSMRailwayType { lazy val value = "monorail" }

  case object NARROW_GAUGE extends OSMRailwayType { lazy val value = "narrow_gauge" }

  case object PRESERVED extends OSMRailwayType { lazy val value = "preserved" }

  case object RAIL extends OSMRailwayType { lazy val value = "rail" }

  case object SUBWAY extends OSMRailwayType { lazy val value = "subway" }

  case object TRAM extends OSMRailwayType { lazy val value = "tram" }

  case object ND extends OSMRailwayType { lazy val value = "N.D" }
}
