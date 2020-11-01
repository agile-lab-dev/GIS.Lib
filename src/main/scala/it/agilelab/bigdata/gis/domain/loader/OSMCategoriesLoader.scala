package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{Geometry, MultiPolygon}
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.CategoriesCfg.CategoryInfoCfg
import it.agilelab.bigdata.gis.domain.models.{OSMGeoCategory, OSMGeoMetadata}


class OSMCategoriesLoader(categoryInfoCfg: CategoryInfoCfg) extends Loader[OSMGeoCategory] {
  import OSMCategoriesLoader._

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    val pattern = """[ \w-]+?(?=\.)""".r

    val fileName: String = source.split("/").reverse.head

    val res: Seq[(Array[AnyRef], MultiPolygon)] =
      ShapeFileReader.readMultiPolygonFeatures(source).map(e => (e._2.toArray, e._1))

    /* fixme
    We need to propagate the country name to the object mapping function, called after the current one.
    This is a terrible hack. I'm gonna refactor it soon.
     */

    val resWithCountryNameInFields =
      res.map(
        pair => {
          val fieldsWithCountryName = pair._1 :+ (pattern findFirstIn fileName).getOrElse(UNKNOWN_NAME)
          val multipolygon = pair._2
          (fieldsWithCountryName, multipolygon)
        }
      )

    resWithCountryNameInFields.toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], geometry: Geometry): OSMGeoCategory = {
    val indexPos = categoryInfoCfg.geometryDescriptionIndex

    val description = if (fields.isDefinedAt(indexPos)) fields(indexPos) else fields.last

    OSMGeoCategory(
      categoryInfoCfg.label,
      OSMGeoMetadata(
        categoryInfoCfg.geoMeta.foldLeft(Map[String, AnyRef]()) {
          (acc, t) => acc ++ (if (fields.isDefinedAt(t._1)) Map(t._2.rawData -> fields(t._1)) else Map.empty)
        } + (DESCRIPTION_META_KEY -> description),
        description.toString),
      geometry
    )

  }

}

object OSMCategoriesLoader {
  private final val UNKNOWN_NAME = "Unknown_name"
  private final val DESCRIPTION_META_KEY = "Description"
}


