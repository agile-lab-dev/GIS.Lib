package it.agilelab.bigdata.gis.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.models.OSMBoundary
import it.agilelab.bigdata.gis.spatialList.GeometryList

object OSMAdministrativeBoundariesLoader{

    //Pay attention to side effects

    var index: GeometryList[OSMBoundary] = null
    def getStreetIndex(path: String) = {
      if (index == null){
        index = new OSMAdministrativeBoundariesLoader().loadIndex(path)
      }
      index
    }

}

class OSMAdministrativeBoundariesLoader() extends Loader[OSMBoundary]{
  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    ShapeFileReader.readMultiPolygonFeatures(source).map(e => (e._2.toArray, e._1)).toIterator
  }

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): OSMBoundary = {

    val name: String = if (fields(14) != null) fields(14).toString.replace("it:","").replace("(Italia)", "") else ""

    OSMBoundary(line, name)

  }


}
