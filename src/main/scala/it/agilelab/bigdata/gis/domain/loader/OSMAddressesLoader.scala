package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{Geometry, Point}
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.OSMAddress
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList


object OSMAddressesLoader{

  var index: GeometryList[OSMAddress] = _

  def getAddressesIndex(path: String): GeometryList[OSMAddress] = {
    if (index == null){
      index = new OSMAddressesLoader().loadIndex(path)
    }
    index
  }

}

class OSMAddressesLoader() extends Loader[OSMAddress]{
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    val lines: Iterator[(Array[AnyRef], Point)] =
      ShapeFileReader.readPointFeatures(source).map(e => (e._2.toArray, e._1)).toIterator
    lines
  }

  protected def objectMapping(fields: Array[AnyRef], point: Geometry): OSMAddress = {

    val street: String = if (fields(2) != null) fields(2).toString else ""
    val number: String = if (fields(3) != null) fields(3).toString else ""

    OSMAddress(point,street,number)
  }

}
