package it.agilelab.bigdata.gis.core

import com.typesafe.config.{Config, ConfigFactory}
import it.agilelab.bigdata.gis.core.apps.ConverterFromOSMToGraphHopperMap
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.graphhopper.{GPSPoint, GraphHopperManager}
import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}

import java.io.File
import java.nio.file.Paths

/**
 * @author andreaL
 */
//Before run this test read Readme section `Test GraphHopper`,
//download file `italy-latest.osm.pbf` and insert in test/resources/graphHopperSource/

class GraphHopperSpec extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll with Logger {

  val conf: Config = ConfigFactory.load()
  val graphConf: Config = conf.getConfig("graph")
  var manager: GraphHopperManager = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    val basePath =  Paths.get("src/test/resources/").toFile.getAbsoluteFile

    val pbfFilePath: String = Paths.get("src/test/resources/graphHopperSource/italy-latest.osm.pbf").toFile.getAbsolutePath
    val graphPath: String = s"$basePath/graphHopper"
    val graphPathOutput: File = Paths.get(graphPath).toFile

    if (graphPathOutput.exists()) {
      logger.info("Graph already exist")
    } else {
      logger.info("Graph not exist, create it")
      graphPathOutput.mkdir()

      ConverterFromOSMToGraphHopperMap.main(
        Array(
          "--graphLocation",
          s"$graphPath",
          "--osmLocation",
          s"$pbfFilePath"
        )
      )
    }

    logger.info(s"Init graph from $graphPathOutput")

    manager = GraphHopperManager(graphConf)
  }

  "test carFlagEncoderEnrich" should "retrieve result of map matching and distance for each type of street" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(45.074246, 7.642711, None, 1552910827000L),
      GPSPoint(45.075511, 7.643988, None, 1552910928000L),
      GPSPoint(45.075511, 7.643988, None, 1552910929000L),
      GPSPoint(45.075511, 7.643988, None, 1552910930000L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value

    assert(matchingRoute.length > 150 && matchingRoute.length < 200)
    assert(matchingRoute.getKmType("trunk_link").isSuccess)
    assert(matchingRoute.getKmType("secondary_link").isSuccess)

  }

  "test with point near sea" should "exclude ferries" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(38.12, 13.37, None, 1552910827000L),
      GPSPoint(38.12, 13.367, None, 1552910928000L),
      GPSPoint(38.1303, 13.3645, None, 1552910929000L),
      GPSPoint(38.1302, 13.3644, None, 1552910930000L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value

    assert(matchingRoute.length > 1700 && matchingRoute.length < 2200)
  }

  "test with point near sea or in sea" should "exclude ferries" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(45.341, 12.309, None, 0L),
      GPSPoint(45.341, 12.309, None, 0L),
      GPSPoint(45.345, 12.314, None, 0L),
      GPSPoint(45.349, 12.32, None, 0L),
      GPSPoint(45.353, 12.325, None, 0L),
      GPSPoint(45.36, 12.332, None, 0L),
      GPSPoint(45.369, 12.337, None, 0L),
      GPSPoint(45.376, 12.341, None, 0L),
      GPSPoint(45.385, 12.348, None, 0L),
      GPSPoint(45.391, 12.353, None, 0L),
      GPSPoint(45.398, 12.358, None, 0L),
      GPSPoint(45.405, 12.364, None, 0L),
      GPSPoint(45.411, 12.366, None, 0L),
      GPSPoint(45.415, 12.367, None, 0L),
      GPSPoint(45.416, 12.371, None, 0L),
      GPSPoint(45.418, 12.372, None, 0L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value

    assert(matchingRoute.length > 10500 && matchingRoute.length < 11500)
  }

  "test with point near pedonal area" should "exclude pedonal area" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(45.075757, 7.671996, None, 0L),
      GPSPoint(45.077163, 7.673426, None, 1L),
      GPSPoint(45.078268, 7.675814, None, 2L),
      GPSPoint(45.070384, 7.685628, None, 3L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value

    assert(matchingRoute.length > 1500 && matchingRoute.length < 2500)
  }

  "test with type road is null" should "change in unclassified" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(38.9159, 16.4589, None, 0L),
      GPSPoint(38.9145, 16.4568, None, 0L),
      GPSPoint(38.9101, 16.4363, None, 0L),
      GPSPoint(38.9091, 16.4149, None, 0L),
      GPSPoint(38.9028, 16.3963, None, 0L),
      GPSPoint(38.904, 16.3814, None, 0L),
      GPSPoint(38.9044, 16.3606, None, 0L),
      GPSPoint(38.9014, 16.3395, None, 0L),
      GPSPoint(38.9083, 16.3233, None, 0L),
      GPSPoint(38.9123, 16.3019, None, 0L),
      GPSPoint(38.9157, 16.2795, None, 0L),
      GPSPoint(38.9188, 16.2665, None, 0L),
      GPSPoint(38.9146, 16.268, None, 0L),
      GPSPoint(38.9015, 16.2781, None, 0L),
      GPSPoint(38.884, 16.2807, None, 0L),
      GPSPoint(38.8663, 16.2793, None, 0L),
      GPSPoint(38.8502, 16.2686, None, 0L),
      GPSPoint(38.835, 16.2579, None, 0L),
      GPSPoint(38.8213, 16.2426, None, 0L),
      GPSPoint(38.8055, 16.2312, None, 0L),
      GPSPoint(38.7905, 16.2231, None, 0L),
      GPSPoint(38.7749, 16.2149, None, 0L),
      GPSPoint(38.7731, 16.2138, None, 0L),
      GPSPoint(38.7708, 16.2159, None, 0L),
      GPSPoint(38.7691, 16.2113, None, 0L),
      GPSPoint(38.767, 16.2037, None, 0L),
      GPSPoint(38.7592, 16.1945, None, 0L),
      GPSPoint(38.7516, 16.187, None, 0L),
      GPSPoint(38.7469, 16.1836, None, 0L),
      GPSPoint(38.7445, 16.1794, None, 0L),
      GPSPoint(38.7436, 16.1794, None, 0L),
      GPSPoint(38.7436, 16.1794, None, 0L),
      GPSPoint(38.7428, 16.177, None, 0L),
      GPSPoint(38.7402, 16.1716, None, 0L),
      GPSPoint(38.7381, 16.1667, None, 0L),
      GPSPoint(38.7352, 16.1638, None, 0L),
      GPSPoint(38.734, 16.1628, None, 0L),
      GPSPoint(38.7282, 16.1578, None, 0L),
      GPSPoint(38.7258, 16.1555, None, 0L),
      GPSPoint(38.7258, 16.1555, None, 0L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value

    assert(!matchingRoute.routes.contains("null"))
  }

  "trip" should "retrieve points sorted by timestamp" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(37.9637, 12.6404, None, 1563855394000L),
      GPSPoint(37.9662, 12.6403, None, 1563855520000L),
      GPSPoint(37.9666, 12.6439, None, 1563855590000L),
      GPSPoint(37.9649, 12.6448, None, 1563855660000L),
      GPSPoint(37.9631, 12.6459, None, 1563855740000L),
      GPSPoint(37.962, 12.6481, None, 1563855800000L),
      GPSPoint(37.9615, 12.6486, None, 1563855810000L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value
    val matchingRouteTimestamp = matchingRoute.distanceBetweenPoints.map(_.diffTime)

    val expectedTimestamp =
      gpsPoint.zip(gpsPoint.tail).map { case (a, b) => b.time - a.time }

    assert(matchingRouteTimestamp == expectedTimestamp)
  }

  "test with point to calculate distance " should "calculate distance between points" in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(42.4599, 12.3813, None, 1568268784000L),
      GPSPoint(42.46, 12.3823, None, 1568268786000L)
    )

    val res = manager.matchingRoute(gpsPoint)

    val matchingRoute = res.right.value
    val distanceBetweenPoints = matchingRoute.distanceBetweenPoints.head

    assert(distanceBetweenPoints.diffTime == 2000L)
    assert(distanceBetweenPoints.distance > 75 && distanceBetweenPoints.distance < 85)
  }

}
