package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.OSMAddress
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

object OSMAddressesLoader {

  var index: GeometryList[OSMAddress] = _

  def getAddressesIndex(path: String): GeometryList[OSMAddress] = {
    if (index == null) {
      index = new OSMAddressesLoader().loadIndex(path)
    }
    index
  }
}

class OSMAddressesLoader() extends Loader[OSMAddress] {
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    ShapeFileReader.readPointFeatures(source).map { case (point, list) => list.toArray -> point }.toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], point: Geometry): OSMAddress = {
    OSMAddress(point, Option(fields(2)).map(_.toString).getOrElse(""), Option(fields(3)).map(_.toString).getOrElse(""))
  }
}
