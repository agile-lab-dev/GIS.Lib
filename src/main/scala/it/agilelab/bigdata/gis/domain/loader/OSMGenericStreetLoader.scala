package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{ Geometry, GeometryFactory }
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.core.utils.{ Logger, ManagerUtils }
import it.agilelab.bigdata.gis.domain.models.{
  OSMHouseNumber,
  OSMSmallAddressNumber,
  OSMStreetAndHouseNumber,
  OSMStreetType
}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem
import org.opengis.feature.simple.SimpleFeature

import scala.util.Try

trait OSMGenericStreetLoader extends Loader[OSMStreetAndHouseNumber] with Logger {

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {
    logger.info("Loading file of source {}", source)
    val start = System.currentTimeMillis()
    val r = ShapeFileReader
      .readMultiLineFeatures(source)
      .map { case (multiLine, list) => (Array(list, multiLine), multiLine) }
      .toIterator
    logger.info("Loaded file of source {} in {} ms", source, System.currentTimeMillis() - start)
    r
  }

  // references: https://wiki.openstreetmap.org/wiki/Key:highway
  override def objectMapping(fields: Array[AnyRef], line: Geometry): OSMStreetAndHouseNumber = {

    val features: SimpleFeature = fields(0).asInstanceOf[SimpleFeature]
    val pointsArray = line.getCoordinates.flatMap(coord => Array(coord.x, coord.y))

    val streetType = Try(Option(features.getAttribute("fclass")).map(_.toString).getOrElse("")).toOption
    val street = Try(new String(features.getAttribute("name").toString.getBytes("ISO-8859-1"), "UTF-8")).toOption

    val st: Option[OSMStreetType] = streetType.map(OSMStreetType.fromValue)

    val oneway: Option[Boolean] = Try(features.getAttribute("oneway").toString != "F").toOption
    val speedLimit: Option[Int] = Try(features.getAttribute("maxspeed").toString.toInt).toOption.filter(_ != 0)
    val isBridge: Option[Boolean] = Try(features.getAttribute("bridge").toString != "F").toOption
    val isTunnel: Option[Boolean] = Try(features.getAttribute("tunnel") != "F").toOption

    OSMStreetAndHouseNumber(
      pointsArray,
      street,
      st,
      Seq.empty[OSMSmallAddressNumber],
      speedLimit,
      isBridge,
      isTunnel,
      oneway)
  }
}

object OSMGenericStreetLoader extends Logger {

  def apply(roadsShapeFile: Seq[String], houseNumbers: Seq[String]): OSMGenericStreetLoader = {

    val loadStreets = roadsShapeFile.nonEmpty
    val loadNumbers = houseNumbers.nonEmpty

    loadStreets match {

      case true if loadNumbers =>
        logger.info("Loading OSM house numbers file into GeometryList...")
        val houseNumberLoader = new OSMHouseNumbersLoader
        val houseNumberGeometryList: GeometryList[OSMHouseNumber] = houseNumberLoader.loadIndex(houseNumbers: _*)
        logger.info("Done loading OSM house numbers file into GeometryList!")
        new OSMStreetLoaderWithNumber(houseNumbersFunction(houseNumberGeometryList))

      case true =>
        new OSMStreetLoaderWithoutNumber()

      case false =>
        new OSMEmptyStreetFileLoader()
    }
  }

  /** Return a function that retrieve the houses number and the respective Point for each road of the road index */
  def houseNumbersFunction(
      houseNumberGeometryList: GeometryList[OSMHouseNumber]
  ): (Geometry, String) => Seq[OSMHouseNumber] = {
    val geometryFactory = new GeometryFactory()
    (road: Geometry, streetName: String) =>
      road.getCoordinates.flatMap { pt =>
        KNNQueryMem.spatialQueryWithMaxDistance(
          houseNumberGeometryList,
          geometryFactory.createPoint(pt),
          ManagerUtils.NUMBERS_MAX_DISTANCE
        )
      }.toSeq
  }
}
