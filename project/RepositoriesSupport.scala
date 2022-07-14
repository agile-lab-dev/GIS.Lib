import sbt._
import sbt.Keys._

trait RepositoriesSupport {

  lazy val allResolvers = {
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      "geo" at "https://repo.osgeo.org/repository/release/"
    )
  }
}

object RepositoriesSupport extends RepositoriesSupport
