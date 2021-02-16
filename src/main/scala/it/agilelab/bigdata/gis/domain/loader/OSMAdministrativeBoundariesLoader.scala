package it.agilelab.bigdata.gis.domain.loader

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{Geometry, MultiPolygon}
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.managers.{CountrySettings, PathManager}
import it.agilelab.bigdata.gis.domain.models.OSMBoundary

import scala.util.Try


case class OSMAdministrativeBoundariesLoader(config: Config, pathManager: PathManager) extends Loader[OSMBoundary] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    val countryName: String =
      source
        .split("/")
        .reverse
        .tail
        .head

    val res: Seq[(Array[AnyRef], MultiPolygon)] =
      ShapeFileReader.readMultiPolygonFeatures(source).map(e => (e._2.toArray, e._1))

    /* fixme
    We need to propagate the country name to the object mapping function, called after the current one.
    This is a terrible hack. I'm gonna refactor it soon.
     */

    val resWithCountryNameInFields =
      res.map(
        pair => {
          val fieldsWithCountryName = pair._1 :+ countryName
          val multipolygon = pair._2
          (fieldsWithCountryName, multipolygon)
        }
      )

    resWithCountryNameInFields.toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMBoundary = {

    val countryName = fields.last.toString
    val countrySettings: CountrySettings = pathManager.getCountrySetting(countryName).clean

    val administrativeValue = fields(Try(config.getInt("administrative.value")).getOrElse(3)).toString
    val stringWithSpecialChars = new String(administrativeValue.getBytes("ISO-8859-1"), "UTF-8")
    val administrativeLevel = fields(Try(config.getInt("administrative.level")).getOrElse(8)).toString

    val boundary: OSMBoundary =
      if ( countrySettings.countrySuffixes.contains(administrativeLevel) )
        OSMBoundary(line, None, None, None, Some(stringWithSpecialChars), administrativeLevel, line.getEnvelopeInternal )
      else if ( countrySettings.regionSuffixes.contains(administrativeLevel) )
        OSMBoundary(line, None, None, Some(stringWithSpecialChars), None, administrativeLevel, line.getEnvelopeInternal )
      else if ( countrySettings.countySuffixes.contains(administrativeLevel) )
        OSMBoundary(line, None, Some(stringWithSpecialChars), None, None, administrativeLevel, line.getEnvelopeInternal )
      else if ( countrySettings.citySuffixes.contains(administrativeLevel) )
        OSMBoundary(line, normalizeCityName(stringWithSpecialChars), None, None, None, administrativeLevel, line.getEnvelopeInternal )
      else
        throw new IllegalArgumentException("Not recognized administrative level!")

    boundary

  }

  protected def normalizeCityName(name: AnyRef): Option[String] = {
    Try(
      name.toString.replace("it:","").replace(" (Italia)", "")
    ).toOption
  }

/*
  def getStreetIndex(path: String): GeometryList[OSMBoundary] = {
    loadIndex(path)
  }
*/

}
