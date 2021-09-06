package it.agilelab.gis.core.utils

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader

import scala.util.{ Failure, Success, Try }

object WktConverter {

  private val reader: WKTReaderRich = new WKTReaderRich()

  /** Read a WKT String and convert it into a Geometry
    *
    * @param wkt String in WKT format "GEOMETRY(lat1 long1, lat2 long2, ...)" to be converted
    * @return the correspondent Geometry object
    */
  def converter(wkt: String): Either[WKT_CONVERTER_ERROR, Geometry] = Try {
    reader.read(wkt)
  } match {
    case Success(geom)      => Right(geom)
    case Failure(exception) => Left(INVALID_WKT(exception))
  }

}
