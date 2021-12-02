package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.models.{ OSMRailTrack, OSMRailwayType, OSMUsage }

import scala.util.Try

class OSMRailwayLoader extends Loader[OSMRailTrack] with Logger {

  private[loader] val keyValuePattern = "\"([^\"]+)\"=>\"([^\"]+)\"".r

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    logger.info("Loading file of source {}", source)
    val start = System.currentTimeMillis()
    val r = ShapeFileReader
      .readMultiLineFeatures(source)
      .map { case (multiLine, list) => (list.toArray, multiLine) }
      .toIterator
    logger.info("Loaded file of source {} in {} ms", source, System.currentTimeMillis() - start)
    r
  }

  // references: https://wiki.openstreetmap.org/wiki/Key:railway
  override def objectMapping(fields: Array[AnyRef], line: Geometry): OSMRailTrack = {

    val map = new String(fields(9).toString.getBytes("ISO-8859-1"), "UTF-8")
      .split(",")
      .flatMap {
        case keyValuePattern(key, value) => Some((key, value))
        case kv =>
          logger.debug(
            s"Key-Value not extracted correctly: $kv"
          ) // it can happen when the field "other_tags" has length > 254 chars
          None
      }
      .toMap

    val osmId = Try(Option(fields(1)).map(_.toString).getOrElse("")).toOption
    val railway = Try(new String(fields(2).toString.getBytes("ISO-8859-1"), "UTF-8")).toOption
    val railwayType = Try(map.getOrElse("railway", "")).toOption
    val operator = Try(map.getOrElse("operator", "")).toOption
    val usage = Try(map.getOrElse("usage", "")).toOption

    val rt: Option[OSMRailwayType] = railwayType.map(OSMRailwayType.fromValue)
    val u: Option[OSMUsage] = usage.map(OSMUsage.fromValue)

    OSMRailTrack(line, osmId, railway, rt, operator, u)
  }
}
