package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.OSMPostalCode
import org.opengis.feature.simple.SimpleFeature

case class OSMPostalCodeLoader() extends Loader[OSMPostalCode] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] =
    ShapeFileReader
      .readPointFeatures(source)
      .map { case (point, list) =>
        Array(list, point) -> point
      }
      .toIterator

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMPostalCode = {

    val features: SimpleFeature = fields(0).asInstanceOf[SimpleFeature]

    val postalCodeValue = Option(features.getAttribute("addrpostco").toString)
    val cityValue = Option(features.getAttribute("addrcity").toString)

    OSMPostalCode(
      point = line,
      postalCode = postalCodeValue,
      city = cityValue
    )
  }
}
