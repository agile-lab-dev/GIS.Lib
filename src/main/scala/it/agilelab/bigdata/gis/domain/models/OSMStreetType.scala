package it.agilelab.bigdata.gis.domain.models

trait OSMStreetType {
  def value: String
}

object OSMStreetType {

  case object MOTORWAY extends OSMStreetType { lazy val value = "motorway" }
  case object SECONDARY extends OSMStreetType { lazy val value = "secondary" }
  case object TRUNK extends OSMStreetType { lazy val value = "trunk" }
  case object UNCLASSIFIED extends OSMStreetType { lazy val value = "unclassified" }
  case object TERTIARY extends OSMStreetType { lazy val value = "tertiary" }
  case object PRIMARY_LINK extends OSMStreetType { lazy val value = "primary_link" }
  case object SECONDARY_LINK extends OSMStreetType { lazy val value = "secondary_link" }
  case object TERTIARY_LINK extends OSMStreetType { lazy val value = "tertiary_link" }
  case object LIVING_STREET extends OSMStreetType { lazy val value = "living_street" }
  case object BUS_GUIDEWAY extends OSMStreetType { lazy val value = "bus_guideway" }
  case object ESCAPE extends OSMStreetType { lazy val value = "escape" }
  case object RACEWAY extends OSMStreetType { lazy val value = "raceway" }
  case object ROAD extends OSMStreetType { lazy val value = "road" }
  case object PROPOSED extends OSMStreetType { lazy val value = "proposed" }
  case object CONSTRUCTION extends OSMStreetType { lazy val value = "construction" }
  case object BRIDLEWAY extends OSMStreetType { lazy val value = "bridleway" }
  case object PRIMARY extends OSMStreetType { lazy val value = "primary" }
  case object TRACK extends OSMStreetType { lazy val value = "track" }
  case object RESIDENTIAL extends OSMStreetType { lazy val value = "residential" }
  case object PEDESTRIAN extends OSMStreetType { lazy val value = "pedestrian" }
  case object TRUNK_LINK extends OSMStreetType { lazy val value = "trunk_Link" }
  case object MOTORWAY_LINK extends OSMStreetType { lazy val value = "motorway_Link" }
  case object FOOTWAY extends OSMStreetType { lazy val value = "footway" }
  case object SERVICE extends OSMStreetType { lazy val value = "service" }
  case object PATH extends OSMStreetType { lazy val value = "path" }
  case object CYCLEWAY extends OSMStreetType { lazy val value = "cycleway" }
  case object TRACK_GRADE2 extends OSMStreetType { lazy val value = "track_Grade2" }
  case object STEPS extends OSMStreetType { lazy val value = "steps" }
  case object ND extends OSMStreetType { lazy val value = "N.D" }

  /** Possible values of street type */
  val values = Seq(
    MOTORWAY,
    SECONDARY,
    TRUNK,
    UNCLASSIFIED,
    TERTIARY,
    PRIMARY_LINK,
    SECONDARY_LINK,
    TERTIARY_LINK,
    LIVING_STREET,
    BUS_GUIDEWAY,
    ESCAPE,
    RACEWAY,
    ROAD,
    PROPOSED,
    CONSTRUCTION,
    BRIDLEWAY,
    PRIMARY,
    TRACK,
    RESIDENTIAL,
    PEDESTRIAN,
    TRUNK_LINK,
    MOTORWAY_LINK,
    FOOTWAY,
    SERVICE,
    PATH,
    CYCLEWAY,
    TRACK_GRADE2,
    STEPS,
    ND
  )

  /** Parses DeltaOutputDataMode from input string */
  def fromValue(v: String): OSMStreetType =
    v.toLowerCase.trim match {
      case MOTORWAY.value       => MOTORWAY
      case SECONDARY.value      => SECONDARY
      case TRUNK.value          => TRUNK
      case TERTIARY.value       => TERTIARY
      case PRIMARY_LINK.value   => PRIMARY_LINK
      case SECONDARY_LINK.value => SECONDARY_LINK
      case TERTIARY_LINK.value  => TERTIARY_LINK
      case LIVING_STREET.value  => LIVING_STREET
      case BUS_GUIDEWAY.value   => BUS_GUIDEWAY
      case ESCAPE.value         => ESCAPE
      case RACEWAY.value        => RACEWAY
      case ROAD.value           => ROAD
      case PROPOSED.value       => PROPOSED
      case CONSTRUCTION.value   => CONSTRUCTION
      case BRIDLEWAY.value      => BRIDLEWAY
      case PRIMARY.value        => PRIMARY
      case TRACK.value          => TRACK
      case RESIDENTIAL.value    => RESIDENTIAL
      case PEDESTRIAN.value     => PEDESTRIAN
      case TRUNK_LINK.value     => TRUNK_LINK
      case MOTORWAY_LINK.value  => MOTORWAY_LINK
      case FOOTWAY.value        => FOOTWAY
      case SERVICE.value        => SERVICE
      case PATH.value           => PATH
      case CYCLEWAY.value       => CYCLEWAY
      case TRACK_GRADE2.value   => TRACK_GRADE2
      case STEPS.value          => STEPS
      case UNCLASSIFIED.value   => UNCLASSIFIED
      case _                    => OSMStreetType.ND
    }
}
