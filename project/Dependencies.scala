import sbt.Keys._
import sbt._

trait Dependencies {

  lazy val coreDependencies: Seq[ModuleID] = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.26",
    "org.slf4j" % "slf4j-simple" % "1.7.26",
    "org.rogach" %% "scallop" % "3.5.1",
    "com.typesafe" % "config" % "1.2.1",
    "org.scalaz" %% "scalaz-core" % "7.3.8",
    "com.github.pureconfig" %% "pureconfig" % "0.14.0",
    "org.locationtech.jts" % "jts-core" % "1.19.0",
    "org.locationtech.jts.io" % "jts-io-common" % "1.19.0",
    "org.geotools" % "gt-shapefile" % "31.5" exclude ("com.vividsolutions", "jts-core"),
    "org.geotools" % "gt-metadata" % "31.5" exclude ("com.vividsolutions", "jt-cores"),
    "com.graphhopper" % "graphhopper-core" % "0.13.0" exclude ("com.vividsolutions", "jts-core"),
    "com.graphhopper" % "graphhopper-reader-osm" % "0.13.0" exclude ("com.vividsolutions", "jts-core"),
    "com.graphhopper" % "graphhopper-map-matching-core" % "0.13.0" exclude ("com.vividsolutions", "jts-core"),
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    "org.apache.xmlgraphics" % "xmlgraphics-commons" % "2.6" exclude ("commons-logging", "commons-logging")
  )

  lazy val core: SettingsDefinition = {
    libraryDependencies ++= coreDependencies
  }
}

object Dependencies extends Dependencies
