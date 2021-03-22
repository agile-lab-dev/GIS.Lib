package it.agilelab.bigdata.gis.domain.loader

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.managers.{CountrySettings, PathManager}
import it.agilelab.bigdata.gis.domain.models.OSMBoundary

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
    ShapeFileReader.readMultiPolygonFeatures(source).map { case (multiPolygon, list) =>
      (list.toArray :+ countryName) -> multiPolygon
    }.toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMBoundary = {

    val countryName = fields.last.toString
    val countrySettings: CountrySettings = pathManager.getCountrySetting(countryName).clean

    val administrativeValue = fields(Try(config.getInt("administrative.value")).getOrElse(3)).toString
    val administrativeLevel = fields(Try(config.getInt("administrative.level")).getOrElse(8)).toString

    if (countrySettings.countrySuffixes.contains(administrativeLevel)) {
      OSMBoundary(
        multiPolygon = line,
        country = Some(parseStringName(administrativeValue)),
        countryCode = Try(fields(2).toString).toOption,
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
        countyCode = Try(fields(14).toString.split("-")(1)).toOption,
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
      throw new IllegalArgumentException(
      s"Unrecognized administrative level: $administrativeLevel [administrativeValue: $administrativeValue] in any of $countrySettings"
      )
    }
  }

  protected def parseStringName(string: String): String = {
    new String(string.getBytes("ISO-8859-1"), "UTF-8")
  }
}
