import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._

inThisBuild(
  Seq(
    scalaVersion := scala211,
    organization := "it.agilelab",
    version := "1.7.0",
    scapegoatVersion := "1.4.15",
    RepositoriesSupport.allResolvers,
    WarningAsErrorsSupport.enableWarningAsErrors,
    WarningAsErrorsSupport.relaxWarningsForConsole,
    description := "Gis Library",
    homepage := Some(url("https://github.com/agile-lab-dev/GIS.Lib")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "paolo.platter@agilelab.it",
        name = "Paolo Platter",
        email = "paolo.platter@agilelab.it",
        url = url("http://www.agilelab.it/")
      ),
      Developer(
        id = "andrea.latella@agilelab.it",
        name = "Andrea Latella",
        email = "andrea.latella@agilelab.it",
        url = url("http://www.agilelab.it/")
      ),
      Developer(
        id = "stefano.samele@agilelab.it",
        name = "Stefano Samele",
        email = "stefano.samele@agilelab.it",
        url = url("http://www.agilelab.it/")
      )
    )
  ))

lazy val root = (project in file("."))
  .settings(
    Dependencies.core,
    crossScalaVersions := supportedScalaVersions,
    name := "gis.lib",
    moduleName := name.value
  )

lazy val scala212 = "2.12.16"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala212, scala211)
