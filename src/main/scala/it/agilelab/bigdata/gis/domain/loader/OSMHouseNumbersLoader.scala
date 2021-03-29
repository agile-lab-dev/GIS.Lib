package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.OSMHouseNumber
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

object OSMHouseNumbersLoader {

  var index: GeometryList[OSMHouseNumber] = _

  def getAddressesIndex(path: String): GeometryList[OSMHouseNumber] = {
    if (index == null) {
      index = new OSMHouseNumbersLoader().loadIndex(path)
    }
    index
  }
}

class OSMHouseNumbersLoader() extends Loader[OSMHouseNumber] {
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    ShapeFileReader.readPointFeatures(source).map { case (point, list) => list.toArray -> point }.toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], point: Geometry): OSMHouseNumber = {
    OSMHouseNumber(point, Option(fields(1)).map(_.toString).getOrElse(""))
  }
}
