package it.agilelab.gis.domain.models

trait OSMUsage {
  def value: String
}

object OSMUsage {
  case object MAIN extends OSMUsage { lazy val value = "main" }
  case object BRANCH extends OSMUsage { lazy val value = "branch" }
  case object INDUSTRIAL extends OSMUsage { lazy val value = "industrial" }
  case object MILITARY extends OSMUsage { lazy val value = "military" }
  case object TEST extends OSMUsage { lazy val value = "test" }
  case object TOURISM extends OSMUsage { lazy val value = "tourism" }
  case object ND extends OSMUsage { lazy val value = "N.D" }

  /** Possible values of usage type */
  val values = Seq(
    MAIN,
    BRANCH,
    INDUSTRIAL,
    MILITARY,
    TEST,
    TOURISM,
    ND
  )

  /** Parses from input string */
  def fromValue(v: String): OSMUsage =
    v.toLowerCase.trim match {
      case MAIN.value       => MAIN
      case BRANCH.value     => BRANCH
      case INDUSTRIAL.value => INDUSTRIAL
      case MILITARY.value   => MILITARY
      case TEST.value       => TEST
      case TOURISM.value    => TOURISM
      case _                => OSMUsage.ND
    }
}