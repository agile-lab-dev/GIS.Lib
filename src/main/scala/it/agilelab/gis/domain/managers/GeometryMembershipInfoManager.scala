package it.agilelab.gis.domain.managers

import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.loader.OSMCategoriesLoader
import it.agilelab.gis.domain.managers.GeometryMembershipInfoManager.OSMGeoCategories
import it.agilelab.gis.domain.models.CategoriesCfg.{ CategoryInfoCfg, Country => CountryCfg, Custom => CustomCfg }
import it.agilelab.gis.domain.models.{ CategoriesCfg, CategoryMembershipOutput, InputCategory, OSMGeoCategory }
import it.agilelab.gis.domain.models.InputCategory.{ Country, Custom }
import org.locationtech.jts.geom.{ GeometryFactory, MultiPoint, Point }
import scalaz.Kleisli

import scala.collection.parallel.ParSeq
import scala.util.Try

class GeometryMembershipInfoManager private (categoriesInfo: Map[InputCategory, OSMGeoCategories]) {

  private val geoFactory = new GeometryFactory()

  def getGeoMembershipInfoOf(category: InputCategory, geometry: Point): Seq[CategoryMembershipOutput] =
    findAll(setupCategory(category))(_.covers(geometry))

  def getFirstGeoMembershipInfoOf(category: InputCategory, geometry: Point): Option[CategoryMembershipOutput] =
    findFirst(setupCategory(category))(_.covers(geometry))

  def getGeoMembershipInfoOf(
      category: InputCategory,
      geometry: MultiPoint,
      strictMembership: Boolean = false
  ): Seq[CategoryMembershipOutput] = {
    val shapes = setupCategory(category)
    if (strictMembership && geometry.getCoordinates.exists(p => shapes.forall(_.disjoint(geoFactory.createPoint(p)))))
      Seq()
    else findAll(shapes)(_.intersects(geometry))
  }

  private def findAll(categories: ParSeq[OSMGeoCategory])(matcher: OSMGeoCategory => Boolean) =
    categories
      .filter(matcher)
      .map(toCategoryMembershipOut)
      .toList

  private def findFirst(categories: ParSeq[OSMGeoCategory])(matcher: OSMGeoCategory => Boolean) =
    categories
      .find(matcher)
      .map(toCategoryMembershipOut)

  private def setupCategory(category: InputCategory): ParSeq[OSMGeoCategory] =
    categoriesInfo
      .getOrElse(category, Seq.empty)
      .par

  private val toCategoryMembershipOut: OSMGeoCategory => CategoryMembershipOutput =
    cat => CategoryMembershipOutput(cat.label, cat.geometa.description, cat.geometa.map)

}

object GeometryMembershipInfoManager extends Logger {

  type OSMGeoCategories = Seq[OSMGeoCategory]
  private val SHP_EXTENSION = ".shp"

  def apply(config: CategoriesCfg): Try[GeometryMembershipInfoManager] = {

    val categoryPathListResolver: Kleisli[Try, String, Seq[String]] =
      Kleisli { s =>
        Try(config.geoDataPath.resolve(s).toFile.listFiles().map(_.getAbsolutePath).filter(_.endsWith(SHP_EXTENSION)))
      }

    for {
      groupByCategory <- Try { (cfg: CategoryInfoCfg, input: InputCategory) =>
        categoryPathListResolver(cfg.label).map(files =>
          Map(input -> new OSMCategoriesLoader(cfg).loadObjects(files: _*)))
      }

      catInfo <- config.categoryInfo
        .foldLeft(Try(Map[InputCategory, OSMGeoCategories]())) { (tryAcc, cfg) =>
          for {
            acc <- tryAcc
            x <- cfg match {
              case cfg: CountryCfg => groupByCategory(cfg, Country)
              case cfg: Custom     => groupByCategory(cfg, Custom(cfg.label))
            }
          } yield acc ++ x
        }
    } yield new GeometryMembershipInfoManager(catInfo)

  }

}
