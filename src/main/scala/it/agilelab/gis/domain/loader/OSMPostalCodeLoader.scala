package it.agilelab.gis.domain.loader

import it.agilelab.gis.core.loader.Loader
import it.agilelab.gis.domain.models.OSMBoundary
import org.locationtech.jts.geom.Geometry
import org.opengis.feature.simple.SimpleFeature

case class OSMPostalCodeLoader() extends Loader[OSMBoundary] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] =
    ShapeFileReader
      .readMultiPolygonFeatures(source)
      .map { case (point, list) =>
        Array[AnyRef](list) -> point
      }
      .toIterator

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMBoundary = {

    val features: SimpleFeature = fields(0).asInstanceOf[SimpleFeature]

    val postalCodeValue = Option(features.getAttribute("CAP").toString)

    OSMBoundary(
      multiPolygon = line,
      env = line.getEnvelopeInternal,
      boundaryType = "",
      postalCode = postalCodeValue
    )
  }

  protected def parseStringName(string: String): String =
    new String(string.getBytes("ISO-8859-1"), "UTF-8")
}
