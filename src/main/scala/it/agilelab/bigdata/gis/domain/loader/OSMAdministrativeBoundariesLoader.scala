package it.agilelab.bigdata.gis.domain.loader

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.managers.{ CountrySettings, PathManager }
import it.agilelab.bigdata.gis.domain.models.OSMBoundary
import org.opengis.feature.simple.SimpleFeature

import java.io.File
import scala.util.Try

case class OSMAdministrativeBoundariesLoader(config: Config, pathManager: PathManager) extends Loader[OSMBoundary] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    val countryName: String =
      source
        .replace(File.separator, "/")
        .split("/")
        .reverse
        .tail
        .head

    /* fixme
    We need to propagate the country name to the object mapping function, called after the current one.
    This is a terrible hack. I'm gonna refactor it soon.
     */
    ShapeFileReader
      .readMultiPolygonFeatures(source)
      .map { case (multiPolygon, list) =>
        Array(list, countryName) -> multiPolygon
      }
      .toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMBoundary = {

    val countryName = fields.last.toString
    val countrySettings: CountrySettings = pathManager.getCountrySetting(countryName).clean

    logger.info(s"Country settings $countrySettings")

    val features: SimpleFeature = fields(0).asInstanceOf[SimpleFeature]

    val administrativeValue = features.getAttribute(config.getString("administrative.value")).toString
    val administrativeLevel = features.getAttribute(config.getString("administrative.level")).toString
    if (countrySettings.countrySuffixes.contains(administrativeLevel)) {
      OSMBoundary(
        multiPolygon = line,
        country = Some(parseStringName(administrativeValue)),
        countryCode =
          Try(extractISO(features.getAttribute(config.getString("administrative.country")).toString)).toOption,
        boundaryType = administrativeLevel,
        env = line.getEnvelopeInternal
      )
    } else if (countrySettings.regionSuffixes.contains(administrativeLevel)) {
      OSMBoundary(
        multiPolygon = line,
        region = Some(parseStringName(administrativeValue)),
        boundaryType = administrativeLevel,
        env = line.getEnvelopeInternal
      )
    } else if (countrySettings.countySuffixes.contains(administrativeLevel)) {
      OSMBoundary(
        multiPolygon = line,
        county = Some(parseStringName(administrativeValue)),
        countyCode =
          Try(extractISO(features.getAttribute(config.getString("administrative.county")).toString)).toOption,
        boundaryType = administrativeLevel,
        env = line.getEnvelopeInternal
      )
    } else if (countrySettings.citySuffixes.contains(administrativeLevel)) {
      OSMBoundary(
        multiPolygon = line,
        city = Some(parseStringName(administrativeValue)),
        boundaryType = administrativeLevel,
        env = line.getEnvelopeInternal
      )
    } else {
      logger.info(
        s"Unrecognized administrative level: $administrativeLevel [administrativeValue: $administrativeValue] in any of $countrySettings"
      )
      null
    }
  }

  private def parseStringName(string: String): String =
    new String(string.getBytes("ISO-8859-1"), "UTF-8")

  def extractISO(iso: String): String = {
    val parts = iso.split("-")
    if (parts.length == 1) {
      parts(0)
    } else if (parts.length == 2) {
      parts(1)
    } else {
      iso
    }
  }

}
