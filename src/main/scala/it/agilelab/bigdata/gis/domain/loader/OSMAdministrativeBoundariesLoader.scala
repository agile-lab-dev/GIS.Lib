package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.OSMBoundary
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import scala.util.Try

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

    //field 5 contains the type of administrative level, field(3) the italian name except for 8th administrative level;
    //For this level the italian name is stored in the 14th field.

    fields(5).toString match{
      case "2" => OSMBoundary(line, None, None, None, Try(fields(3).toString).toOption, fields(5).toString )
      case "4" => OSMBoundary(line, None, None, Try(fields(3).toString).toOption, Some("Italia"), fields(5).toString )
      case "6" => OSMBoundary(line, None, Try(fields(3).toString).toOption, None, Some("Italia"), fields(5).toString )
      case "8" => OSMBoundary(line, normalizeCityName(fields(14)), None, None, Some("Italia"), fields(5).toString )
      case "3" => OSMBoundary(line, None, None, None, Try(fields(3).toString).toOption, fields(5).toString )
      case _ => throw new IllegalArgumentException("Not recognized administrative level!")
    }

  }

  protected def normalizeCityName(name: AnyRef) = {
    Try(
      name.toString.replace("it:","").replace(" (Italia)", "")
    ).toOption
  }



}
