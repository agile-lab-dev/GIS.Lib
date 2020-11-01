package it.agilelab.bigdata.gis.domain.loader


import com.vividsolutions.jts.geom.{Geometry, GeometryFactory, MultiLineString}
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.managers.ManagerUtils
import it.agilelab.bigdata.gis.domain.models.{OSMAddress, OSMSmallAddressNumber, OSMStreetEnriched, OSMStreetType}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem

import scala.util.Try

trait OSMGenericStreetLoader extends Loader[OSMStreetEnriched] {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    val lines: Iterator[(Array[AnyRef], MultiLineString)] =
      ShapeFileReader
        .readMultiLineFeatures(source)
        .map(e => (e._2.toArray, e._1)).toIterator

    lines
  }


  // references: https://wiki.openstreetmap.org/wiki/Key:highway
  override def objectMapping(fields: Array[AnyRef],line: Geometry): OSMStreetEnriched = {
    val street = Try(new String(fields(4).toString.getBytes("ISO-8859-1"), "UTF-8")).toOption
    val streetType = Try(if(fields(3) != null) fields(3).toString else "").toOption
    val st: Option[OSMStreetType.Value] =
      streetType
        .map(_.toLowerCase)
        .map {
          case "motorway" => OSMStreetType.Motorway
          case "trunk" => OSMStreetType.Trunk
          case "secondary" => OSMStreetType.Secondary
          case "unclassified" => OSMStreetType.Unclassified
          case "tertiary" => OSMStreetType.Tertiary
          case "primary_link" => OSMStreetType.Primary_link
          case "secondary_link" => OSMStreetType.Secondary_link
          case "tertiary_link" => OSMStreetType.Tertiary_link
          case "living_street" => OSMStreetType.Living_street
          case "bus_guideway" => OSMStreetType.Bus_guideway
          case "escape" => OSMStreetType.Escape
          case "raceway" => OSMStreetType.Raceway
          case "bridleway" => OSMStreetType.Bridleway
          case "road" => OSMStreetType.Road
          case "primary" => OSMStreetType.Primary
          case "track" => OSMStreetType.Track
          case "residential" => OSMStreetType.Residential
          case "pedestrian" => OSMStreetType.Pedestrian
          case "trunk_link" => OSMStreetType.Trunk_Link
          case "motorway_link" => OSMStreetType.Motorway_Link
          case "footway" => OSMStreetType.Footway
          case "service" => OSMStreetType.Service
          case "path" => OSMStreetType.Path
          case "cycleway" => OSMStreetType.Cycleway
          case "track_grade2" => OSMStreetType.Track_Grade2
          case "steps" => OSMStreetType.Steps
          case "proposed" => OSMStreetType.Proposed
          case "construction" => OSMStreetType.Construction
          case _ => OSMStreetType.Unclassified
        }

    val pointsArray = line.getCoordinates.flatMap(coord => Array(coord.x, coord.y))

    OSMStreetEnriched(pointsArray, street, st, Seq.empty[OSMSmallAddressNumber])
  }
}

class OSMGenericStreetLoaderWithoutNumber() extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetEnriched] = {
    val notIndexedStreets: List[OSMStreetEnriched] = loadObjects(sources:_*)
    buildIndex(notIndexedStreets)
  }

}

class OSMGenericStreetLoaderWithNumber(addressesIndex: (Geometry,String) => Seq[OSMAddress]) extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetEnriched] = {
    val notIndexedStreets: Iterator[OSMStreetEnriched] = loadObjects(addressesIndex, sources:_*)
    buildIndex(notIndexedStreets.toList)
  }

  def loadObjects(addressesIndex: (Geometry,String) => Seq[OSMAddress], sources: String*): Iterator[OSMStreetEnriched] = {
    val lines: Iterator[OSMStreetEnriched] =
      sources
        .foldLeft(Seq.empty[OSMStreetEnriched].toIterator)((acc, source) =>
          acc ++ loadFile(source)
            .map(e => {
              val lr: Geometry = e._2
              val fields = e._1
              objectMappingWithAddresses(fields, lr, addressesIndex)
            }))
    lines
  }

  protected def objectMappingWithAddresses(fields: Array[AnyRef], line: Geometry, addressesIndex: (Geometry,String) => Seq[OSMAddress]): OSMStreetEnriched = {
    val osmStreetWithoutNumbers: OSMStreetEnriched = objectMapping(fields, line)

    if(osmStreetWithoutNumbers.street.isDefined) { //In principle, this if should be useless...
      val addressesIndexeFuncResult = addressesIndex(line, osmStreetWithoutNumbers.street.get)
      OSMStreetEnriched.decorateWithNumbers(osmStreetWithoutNumbers, addressesIndexeFuncResult)
    }

    else
      osmStreetWithoutNumbers
  }

}

class OSMMissingStreetFileLoader extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetEnriched] = null

}

object OSMGenericStreetLoader extends Logger {

  def apply(roadsShapeFile: Seq[String], addresses: Seq[String]): OSMGenericStreetLoader = {

    val loadStreets = roadsShapeFile.nonEmpty
    val loadNumber = addresses.nonEmpty

    if (loadStreets) {

      if (loadNumber) {
        logger.info("Loading OSM addresses file into GeometryList...")
        val addressNumberLoader = new OSMAddressesLoader
        val addressNumberGeometryList: GeometryList[OSMAddress] = addressNumberLoader.loadIndex(addresses: _*)
        logger.info("Done loading OSM addresses file into GeometryList!")

        /**
         * function that returns the addresses number and the respective Point for each road of the road index
         */
        val retrieveAddressNumbers: (Geometry,String) => Seq[OSMAddress] =
          (road: Geometry, streetName: String) => {

            road.getCoordinates
              .flatMap( pt => {
                KNNQueryMem.spatialQueryWithMaxDistance(
                  addressNumberGeometryList,
                  new GeometryFactory().createPoint(pt),
                  ManagerUtils.NUMBERS_MAX_DISTANCE)
              })
              //THIS EQUALS WORKS BECAUSE THE SHP OF THE ROADS AND ADDRESSES ARE BOTH OSM AND HAVE THE SAME STREET NAMES
              //IN CASE OF ETHEROGENEOUS SOURCES THIS SHOULD BE CHANGE WITH A STRING SIMILARITY ALGORITHM
              .filter(address => {address.street.trim.equalsIgnoreCase(streetName.trim)})
              .toSeq
          }

        new OSMGenericStreetLoaderWithNumber(retrieveAddressNumbers)
      }

      else {
        new OSMGenericStreetLoaderWithoutNumber()
      }
    }
    else {
      new OSMMissingStreetFileLoader()
    }

  }

}

