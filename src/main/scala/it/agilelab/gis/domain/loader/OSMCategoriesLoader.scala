package it.agilelab.gis.domain.loader

import java.io.File
import java.util.regex.Pattern

import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.domain.models.CategoriesCfg.CategoryInfoCfg
import it.agilelab.gis.domain.models.{ OSMGeoCategory, OSMGeoMetadata }
import org.locationtech.jts.geom.Geometry

class OSMCategoriesLoader(categoryInfoCfg: CategoryInfoCfg) extends Loader[OSMGeoCategory] {

  import OSMCategoriesLoader._

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    val pattern = """[ \w-]+?(?=\.)""".r

    val fileName: String = source.split(Pattern.quote(File.separator)).reverse.head

    /* fixme
     We need to propagate the country name to the object mapping function, called after the current one.
     This is a terrible hack. I'm gonna refactor it soon.
     */

    ShapeFileReader
      .readMultiPolygonFeatures(source)
      .map { case (multiPolygon, list) =>
        (list.getAttributes.toArray :+ (pattern findFirstIn fileName).getOrElse(UNKNOWN_NAME)) -> multiPolygon
      }
      .toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMGeoCategory = {
    val indexPos = categoryInfoCfg.geometryDescriptionIndex

    val description = if (fields.isDefinedAt(indexPos)) fields(indexPos) else fields.last

    OSMGeoCategory(
      categoryInfoCfg.label,
      OSMGeoMetadata(
        categoryInfoCfg.geoMeta.foldLeft(Map[String, AnyRef]()) { (acc, t) =>
          acc ++ (if (fields.isDefinedAt(t._1)) Map(t._2.rawData -> fields(t._1)) else Map.empty)
        } + (DESCRIPTION_META_KEY -> description),
        description.toString
      ),
      geometry
    )

  }

}

object OSMCategoriesLoader {
  private final val UNKNOWN_NAME = "Unknown_name"
  private final val DESCRIPTION_META_KEY = "Description"
}
