package it.agilelab.gis.core.utils

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader

import scala.util.Try

object WktConverter {

  private val reader: WKTReader = new WKTReader()

  /** Read a WKT String and convert it into a Geometry
    *
    * @param wkt String in WKT format "GEOMETRY(lat1 long1, lat2 long2, ...)" to be converted
    * @return the correspondent Geometry object
    */
  def wktConverter(wkt: String): Try[Geometry] = Try {
    reader.read(wkt)
  }

}
