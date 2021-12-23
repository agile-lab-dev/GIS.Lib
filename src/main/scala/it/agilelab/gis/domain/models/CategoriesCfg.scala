package it.agilelab.gis.domain.models

import CategoriesCfg.CategoryInfoCfg
import it.agilelab.gis.core.utils.ConfigurationProperties.GEOCODE
import pureconfig._
import pureconfig.configurable._
import pureconfig.ConvertHelpers._
import pureconfig.generic.auto._

import java.nio.file.Path
import scala.util.{ Failure, Success, Try }

case class CategoriesCfg(geoDataPath: Path, categoryInfo: Seq[CategoryInfoCfg])

object CategoriesCfg {

  implicit val intMapIndexReader: ConfigReader[Map[Int, GeoMetadataCfg]] =
    genericMapReader[Int, GeoMetadataCfg](catchReadError(_.toInt))

  case class GeoMetadataCfg(rawData: String) extends AnyVal

  sealed trait CategoryInfoCfg {
    def label: String

    def geoMeta: Map[Int, GeoMetadataCfg]

    def geometryDescriptionIndex: Int
  }

  case class Country(geoMeta: Map[Int, GeoMetadataCfg], geometryDescriptionIndex: Int) extends CategoryInfoCfg {
    def label: String = "country"
  }

  case class Custom(label: String, geoMeta: Map[Int, GeoMetadataCfg], geometryDescriptionIndex: Int)
      extends CategoryInfoCfg

  def load: Try[CategoriesCfg] =
    ConfigSource.default
      .at(s"${GEOCODE.value}.categories")
      .load[CategoriesCfg]
      .fold(
        l => Failure(new Throwable(l.toList.map(_.description).fold("")(_ ++ "\n" ++ _))),
        Success(_)
      )

}
