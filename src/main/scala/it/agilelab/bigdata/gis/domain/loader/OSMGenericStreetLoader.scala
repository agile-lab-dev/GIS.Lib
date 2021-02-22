package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{Geometry, GeometryFactory}
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.core.utils.{Logger, ManagerUtils}
import it.agilelab.bigdata.gis.domain.models.{OSMAddress, OSMSmallAddressNumber, OSMStreetAndHouseNumber, OSMStreetType}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem

import scala.util.Try

trait OSMGenericStreetLoader extends Loader[OSMStreetAndHouseNumber] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    ShapeFileReader
      .readMultiLineFeatures(source)
      .map { case (multiLine, list) => (list.toArray, multiLine) }
      .toIterator
  }

  // references: https://wiki.openstreetmap.org/wiki/Key:highway
  override def objectMapping(fields: Array[AnyRef], line: Geometry): OSMStreetAndHouseNumber = {
    val pointsArray = line.getCoordinates.flatMap(coord => Array(coord.x, coord.y))

    val osm_id = fields(1).toString
    val streetType = Try(Option(fields(3)).map(_.toString).getOrElse("")).toOption
    val street = Try(new String(fields(4).toString.getBytes("ISO-8859-1"), "UTF-8")).toOption

    val st: Option[OSMStreetType] = streetType.map(OSMStreetType.fromValue)

    val oneway: Option[Boolean] = Try(fields(6).toString != "F").toOption
    val speedLimit: Option[Int] = Try(fields(7).toString.toInt).toOption.filter(_ != 0)
    val isBridge: Option[Boolean] = Try(fields(9).toString != "F").toOption
    val isTunnel: Option[Boolean] = Try(fields(10).toString != "F").toOption
    //val toSpeed: Integer = fields(6).toInt

    OSMStreetAndHouseNumber(osm_id, pointsArray, street, st, Seq.empty[OSMSmallAddressNumber], speedLimit, isBridge, isTunnel, oneway)
  }
}

object OSMGenericStreetLoader extends Logger {

  def apply(roadsShapeFile: Seq[String], addresses: Seq[String]): OSMGenericStreetLoader = {

    val loadStreets = roadsShapeFile.nonEmpty
    val loadNumber = addresses.nonEmpty

    loadStreets match {

      case true if loadNumber =>
        logger.info("Loading OSM addresses file into GeometryList...")
        val addressNumberLoader = new OSMAddressesLoader
        val addressNumberGeometryList: GeometryList[OSMAddress] = addressNumberLoader.loadIndex(addresses: _*)
        logger.info("Done loading OSM addresses file into GeometryList!")
        new OSMStreetLoaderWithNumber(addressFunction(addressNumberGeometryList))

      case true =>
        new OSMStreetLoaderWithoutNumber()

      case false =>
        new OSMEmptyStreetFileLoader()
    }
  }

  /** Return a function that retrieve the addresses number and the respective Point for each road of the road index */
  def addressFunction(addressNumberGeometryList: GeometryList[OSMAddress]): (Geometry, String) => Seq[OSMAddress] = {
    (road: Geometry, streetName: String) => {
      road.getCoordinates
        .flatMap(pt => {
          KNNQueryMem.spatialQueryWithMaxDistance(
            addressNumberGeometryList,
            new GeometryFactory().createPoint(pt),
            ManagerUtils.NUMBERS_MAX_DISTANCE)
        })
        //THIS EQUALS WORKS BECAUSE THE SHP OF THE ROADS AND ADDRESSES ARE BOTH OSM AND HAVE THE SAME STREET NAMES
        //IN CASE OF ETHEROGENEOUS SOURCES THIS SHOULD BE CHANGE WITH A STRING SIMILARITY ALGORITHM
        .filter(address => {
          address.street.trim.equalsIgnoreCase(streetName.trim)
        })
        .toSeq
    }
  }
}

