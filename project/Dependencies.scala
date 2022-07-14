import sbt.Keys._
import sbt._

trait Dependencies {

  lazy val coreDependencies: Seq[ModuleID] = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.26",
    "org.slf4j" % "slf4j-simple" % "1.7.26",
    "org.rogach" %% "scallop" % "3.5.1",
    "com.typesafe" % "config" % "1.2.1",
    "org.scalaz" %% "scalaz-core" % "7.3.2",
    "com.github.pureconfig" %% "pureconfig" % "0.12.1",
    "org.datasyslab" % "JTSplus" % "0.1.0",
    "org.geotools" % "gt-shapefile" % "17.2" exclude ("com.vividsolutions", "jts"),
    "org.wololo" % "jts2geojson" % "0.7.0" excludeAll (
      ExclusionRule(organization = "com.vividsolutions", name = "jts"),
      ExclusionRule(organization = "com.fasterxml.jackson.core", name = "jackson-databind"),
      ExclusionRule(organization = "com.fasterxml.jackson.core", name = "jackson-core")
    ),
    "com.graphhopper" % "graphhopper-core" % "0.11.0" exclude ("com.vividsolutions", "jts-core"),
    "com.graphhopper" % "graphhopper-reader-osm" % "0.11.0",
    "com.graphhopper" % "graphhopper-map-matching-core" % "0.11.0-4",
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    "org.apache.xmlgraphics" % "xmlgraphics-commons" % "2.6" exclude ("commons-logging", "commons-logging")
  )

  lazy val core: SettingsDefinition = {
    libraryDependencies ++= coreDependencies
  }
}

object Dependencies extends Dependencies
