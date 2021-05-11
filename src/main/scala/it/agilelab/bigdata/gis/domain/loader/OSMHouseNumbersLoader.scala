package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.models.OSMHouseNumber
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import org.opengis.feature.simple.SimpleFeature

object OSMHouseNumbersLoader {

  var index: GeometryList[OSMHouseNumber] = _

  def getAddressesIndex(path: String): GeometryList[OSMHouseNumber] = {
    if (index == null) {
      index = new OSMHouseNumbersLoader().loadIndex(path)
    }
    index
  }
}

class OSMHouseNumbersLoader() extends Loader[OSMHouseNumber] with Logger {
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] =
    ShapeFileReader.readPointFeatures(source).map { case (point, list) => Array(list, point) -> point }.toIterator

  protected def objectMapping(fields: Array[AnyRef], point: Geometry): OSMHouseNumber = {
    val features: SimpleFeature = fields(0).asInstanceOf[SimpleFeature]
    val houseNumber = features.getAttribute("addrhousen")
    OSMHouseNumber(point, Option(houseNumber).map(_.toString).getOrElse(""))
  }
}
