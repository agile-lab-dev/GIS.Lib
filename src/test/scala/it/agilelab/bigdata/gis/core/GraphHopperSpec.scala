package it.agilelab.bigdata.gis.core

import java.io.File

import com.graphhopper.util.GPXEntry
import it.agilelab.bigdata.gis.core.apps.ConverterFromOSMToGraphHopperMap
import it.agilelab.bigdata.gis.domain.graphhopper.GraphHopperManager
import org.apache.log4j.{Level, Logger}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.collection.JavaConversions._


/**
 * @author andreaL
 */
class GraphHopperSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  Logger.getRootLogger.setLevel(Level.INFO)
  val logger: Logger = Logger.getLogger(getClass)

  override def beforeAll(): Unit = {
    super.beforeAll()
    val basePath  = getClass.getResource("/").getPath

    val pbfFilePath: String = s"$basePath/graphHopperSource/italy-latest.osm.pbf"
    val graphPath: String = s"$basePath/graphHopper"
    val graphPathOutput = new File(graphPath)

      if( graphPathOutput.exists()){
        logger.info("Graph already exist")
        getClass.getResource("/graphHopper/").getPath
      }
      else {
        logger.info("Graph not exist, create it")
        graphPathOutput.mkdir()

        ConverterFromOSMToGraphHopperMap.main(
          Array(
            "--graphLocation",
            s"$graphPath",
            "--osmLocation",
            s"$pbfFilePath")
        )
      }

    logger.info(s"Init graph from $graphPathOutput")

    GraphHopperManager.init(s"${graphPathOutput.getAbsolutePath}")
  }

  "test carFlagEncoderEnrich" should "retrieve result of map matching and distance for each type of street" in {

    val gpsPoint:List[GPXEntry] = List(
      new GPXEntry(45.074246, 7.642711, 1552910827000L),
      new GPXEntry(45.075511, 7.643988, 1552910928000L),
      new GPXEntry(45.075511, 7.643988, 1552910929000L),
      new GPXEntry(45.075511, 7.643988, 1552910930000L)
    )

    val res = GraphHopperManager.matchingRoute(gpsPoint)

    assert(res.length > 150 && res.length < 200)
    assert(res.getKmType("trunk_link").isSuccess)
    assert(res.getKmType("secondary_link").isSuccess)

  }

  "test with point near sea" should "exclude ferries" in {

    val gpsPoint:List[GPXEntry] = List(
      new GPXEntry(38.12,13.37, 1552910827000L),
      new GPXEntry(38.12,13.367, 1552910928000L),
      new GPXEntry(38.1303,13.3645, 1552910929000L),
      new GPXEntry(38.1302,13.3644, 1552910930000L)

    )

    val res = GraphHopperManager.matchingRoute(gpsPoint)

    println(res.length)
    assert(res.length > 2500 && res.length < 3500)

  }


  "test with point near sea or in sea" should "exclude ferries"  in {

    val gpsPoint:List[GPXEntry] = List(
      new GPXEntry(45.341,	12.309, 0L),
      new GPXEntry(45.341,	12.309, 0L),
      new GPXEntry(45.345,	12.314, 0L),
      new GPXEntry(45.349,	12.32, 0L),
      new GPXEntry(45.353,	12.325, 0L),
      new GPXEntry(45.36	, 12.332, 0L),
      new GPXEntry(45.369,	12.337, 0L),
      new GPXEntry(45.376,	12.341, 0L),
      new GPXEntry(45.385,	12.348, 0L),
      new GPXEntry(45.391,	12.353, 0L),
      new GPXEntry(45.398,	12.358, 0L),
      new GPXEntry(45.405,	12.364, 0L),
      new GPXEntry(45.411,	12.366, 0L),
      new GPXEntry(45.415,	12.367, 0L),
      new GPXEntry(45.416,	12.371, 0L),
      new GPXEntry(45.418,	12.372, 0L)
    )

    val res = GraphHopperManager.matchingRoute(gpsPoint)
    println(s"Length: ${res.length}")
    assert(res.length > 10000 && res.length < 11000)
  }

  "test with point near pedonal area" should "exclude pedonal area"  in {

    val gpsPoint:List[GPXEntry] = List(
      new GPXEntry(45.075757, 7.671996, 0L),
      new GPXEntry(45.077163, 7.673426, 1L),
      new GPXEntry(45.078268, 7.675814, 2L),
      new GPXEntry(45.070384, 7.685628, 3L)
    )

    val res = GraphHopperManager.matchingRoute(gpsPoint)
    println(s"Length: ${res.length}")

    assert(res.length > 1500 && res.length < 2500)

  }

  "test with type road is null" should "change in unclassified"  in {

    val gpsPoint:List[GPXEntry] = List(
      new GPXEntry(38.9159,16.4589,0L),
      new GPXEntry(38.9145,16.4568,0L),
      new GPXEntry(38.9101,16.4363,0L),
      new GPXEntry(38.9091,16.4149,0L),
      new GPXEntry(38.9028,16.3963,0L),
      new GPXEntry(38.904,16.3814,0L),
      new GPXEntry(38.9044,16.3606,0L),
      new GPXEntry(38.9014,16.3395,0L),
      new GPXEntry(38.9083,16.3233,0L),
      new GPXEntry(38.9123,16.3019,0L),
      new GPXEntry(38.9157,16.2795,0L),
      new GPXEntry(38.9188,16.2665,0L),
      new GPXEntry(38.9146,16.268,0L),
      new GPXEntry(38.9015,16.2781,0L),
      new GPXEntry(38.884,16.2807,0L),
      new GPXEntry(38.8663,16.2793,0L),
      new GPXEntry(38.8502,16.2686,0L),
      new GPXEntry(38.835,16.2579,0L),
      new GPXEntry(38.8213,16.2426,0L),
      new GPXEntry(38.8055,16.2312,0L),
      new GPXEntry(38.7905,16.2231,0L),
      new GPXEntry(38.7749,16.2149,0L),
      new GPXEntry(38.7731,16.2138,0L),
      new GPXEntry(38.7708,16.2159,0L),
      new GPXEntry(38.7691,16.2113,0L),
      new GPXEntry(38.767,16.2037,0L),
      new GPXEntry(38.7592,16.1945,0L),
      new GPXEntry(38.7516,16.187,0L),
      new GPXEntry(38.7469,16.1836,0L),
      new GPXEntry(38.7445,16.1794,0L),
      new GPXEntry(38.7436,16.1794,0L),
      new GPXEntry(38.7436,16.1794,0L),
      new GPXEntry(38.7428,16.177,0L),
      new GPXEntry(38.7402,16.1716,0L),
      new GPXEntry(38.7381,16.1667,0L),
      new GPXEntry(38.7352,16.1638,0L),
      new GPXEntry(38.734,16.1628,0L),
      new GPXEntry(38.7282,16.1578,0L),
      new GPXEntry(38.7258,16.1555,0L),
      new GPXEntry(38.7258,16.1555,0L))


    val res = GraphHopperManager.matchingRoute(gpsPoint)
    println(res.routes)
    assert(res.routes.get("null").isEmpty)
  }



}
