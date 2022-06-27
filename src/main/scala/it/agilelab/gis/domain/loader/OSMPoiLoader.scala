package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.core.utils.Logger

import scala.util.Try

trait OSMPoiLoader[T <: Geometry] extends Loader[T] with Logger {

  private[loader] val keyValuePattern = "\"([^\"]+)\"=>\"([^\"]+)\"".r

  override protected def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    logger.info("Loading file of source {}", source)
    val start = System.currentTimeMillis()
    val geometries = source match {
      case l if l contains "lines" =>
        ShapeFileReader.readMultiLineFeatures(source)
      case mp if mp contains "multipolygons" =>
        ShapeFileReader.readMultiPolygonFeatures(source).map { case (mp, sp) => (mp, sp.getAttributes) }
      case p if p contains "points" =>
        ShapeFileReader.readPointFeatures(source)
      case unk =>
        logger.error("Discarding file with unknown geometry: {}", unk)
        Seq.empty
    }
    val res = geometries.map { case (geometry, list) => (list.toArray, geometry) }.toIterator
    logger.info("Loaded file of source {} in {} ms", source, System.currentTimeMillis() - start)
    res
  }

  protected def getInfosFromOtherTags(
      fields: Array[AnyRef],
      fieldIndex: Int,
      key: String
  ): Option[String] = {
    val optionalMap = Try(
      new String(fields(fieldIndex).toString.getBytes("ISO-8859-1"), "UTF-8")
        .split(",")
        .flatMap {
          case keyValuePattern(k, value) => Some((k, value))
          case kv =>
            logger.debug(
              s"Key-Value not extracted correctly: $kv"
            ) // it can happen when the field "other_tags" has length > 254 chars
            None
        }
        .toMap).toOption
    optionalMap.getOrElse(Map.empty[String, String]).get(key)
  }

}
