import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import xerial.sbt.Sonatype._

inThisBuild(
  Seq(
    scalaVersion := scala212,
    organization := "it.agilelab",
    version := "1.10.0-SNAPSHOT",
    scapegoatVersion := "1.4.15",
    RepositoriesSupport.allResolvers,
    WarningAsErrorsSupport.enableWarningAsErrors,
    WarningAsErrorsSupport.relaxWarningsForConsole,
    // Legacy OSSRH (oss.sonatype.org) has been decommissioned by Sonatype.
    // Publishing must now go through the new Sonatype Central Publishing Portal.
    sonatypeCredentialHost := sonatypeCentralHost,
    description := "Gis Library",
    homepage := Some(url("https://github.com/agile-lab-dev/GIS.Lib")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    // Required by the new Sonatype Central Publishing Portal POM validation
    // (missing scm block causes STAGE_FAILURE on deployment).
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/agile-lab-dev/GIS.Lib"),
        "scm:git:https://github.com/agile-lab-dev/GIS.Lib.git",
        Some("scm:git:git@github.com:agile-lab-dev/GIS.Lib.git")
      )
    ),
    versionScheme := Some("early-semver"),
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
lazy val supportedScalaVersions = List(scala212)
