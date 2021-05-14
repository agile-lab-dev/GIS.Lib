package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.domain.models.OSMPostalCode

case class OSMPostalCodeLoader() extends Loader[OSMPostalCode] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] =
    ShapeFileReader
      .readPointFeatures(source)
      .map { case (point, list) =>
        (list.toArray) -> point
      }
      .toIterator

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMPostalCode = {

    val postalCodeValue = Option(fields(1).toString)
    val cityValue = Option(fields(2).toString)

    OSMPostalCode(
      point = line,
      postalCode = postalCodeValue,
      city = cityValue
    )
  }

  protected def parseStringName(string: String): String =
    new String(string.getBytes("ISO-8859-1"), "UTF-8")
}
