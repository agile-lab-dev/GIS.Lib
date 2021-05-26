package it.agilelab.gis.core

import com.typesafe.config.{ Config, ConfigFactory }
import it.agilelab.gis.core.apps.ConverterFromOSMToGraphHopperMap
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.graphhopper.{ GPSPoint, GraphHopperManager, MatchedRoute, TracePoint }
import org.scalatest._
import org.scalatest.tagobjects.Slow

import java.io.File
import java.nio.file.Paths

/** @author andreaL
  */
class GraphHopperSpec
    extends FlatSpec
    with Matchers
    with EitherValues
    with TryValues
    with BeforeAndAfterAll
    with Logger {

  val conf: Config = ConfigFactory.load()
  val graphConf: Config = conf.getConfig("graph")
  var manager: GraphHopperManager = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    val basePath = Paths.get("src/test/resources/").toFile.getAbsoluteFile

    val pbfFilePath: String =
      Paths.get("src/test/resources/graphHopperSource/milan.osm.pbf").toFile.getAbsolutePath
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

  "Given 1 point in Milan in a residential road" should "return a matched route" in {
    val time = 1619780763562L
    val point1 = GPSPoint(45.46615, 9.18700, None, time)

    val points = Seq(point1)

    val response = manager.matchingRoute(points).right.value

    val expected = MatchedRoute(
      points = Seq(
        TracePoint(
          latitude = 45.46615,
          longitude = 9.18700,
          altitude = None,
          time = time,
          matchedLatitude = Some(45.46615464688791),
          matchedLongitude = Some(9.18699887809106),
          matchedAltitude = Some(0.0),
          roadType = Some("residential"),
          roadName = Some("Via Bassano Porrone"),
          speedLimit = Some(30),
          linearDistance = Some(0.5240652051613223)
        )),
      length = 0.0,
      time = 0,
      routes = Map("residential" -> 99.406),
      distanceBetweenPoints = Seq()
    )

    noneAltitude(response) shouldBe expected
  }

  "Given 2 point in Milan in a motorway road" should "return a matched route" in {

    val point1 = GPSPoint(45.48237, 9.25148, None, 1619275184000L)
    val point2 = GPSPoint(45.48271, 9.25139, None, 1619275191000L)

    val points = Seq(point1, point2)

    val response = manager.matchingRoute(points).right.value

    val expected = MatchedRoute(
      points = Seq(
        TracePoint(
          latitude = 45.48237,
          longitude = 9.25148,
          altitude = None,
          time = 1619275184000L,
          matchedLatitude = Some(45.48237218105361),
          matchedLongitude = Some(9.251493186573803),
          matchedAltitude = Some(0.0),
          roadType = Some("motorway"),
          roadName = Some("Tangenziale Est, A51"),
          speedLimit = Some(80),
          linearDistance = Some(1.0562698124311882)
        ),
        TracePoint(
          latitude = 45.48271,
          longitude = 9.25139,
          altitude = None,
          time = 1619275191000L,
          matchedLatitude = Some(45.48270908694192),
          matchedLongitude = Some(9.25138280885055),
          matchedAltitude = Some(0.0),
          roadType = Some("motorway"),
          roadName = Some("Tangenziale Est, A51"),
          speedLimit = Some(80),
          linearDistance = Some(0.5697515483264242)
        )
      ),
      length = 38.44312467891209,
      time = 1729,
      routes = Map("motorway" -> 901.385),
      distanceBetweenPoints = Seq()
    )

    noneAltitude(response) shouldBe expected
  }

  "Given 1 point" should "return a matched route" taggedAs Slow in {

    val time = 1619780763562L
    val point1 = GPSPoint(45.0663115866474, 7.637293092568119, None, time)

    val points = Seq(point1)

    val response = manager.matchingRoute(points).right.value

    val expected = MatchedRoute(
      points = Seq(
        TracePoint(
          latitude = 45.0663115866474,
          longitude = 7.637293092568119,
          altitude = None,
          time = time,
          matchedLatitude = Some(45.066316193850604),
          matchedLongitude = Some(7.637272156733646),
          matchedAltitude = Some(0.0),
          roadType = Some("primary"),
          roadName = Some("Corso Trapani"),
          speedLimit = Some(50),
          linearDistance = Some(1.7221707379444537)
        )),
      length = 0.0,
      time = 0,
      routes = Map("primary" -> 470.687),
      distanceBetweenPoints = Seq()
    )

    noneAltitude(response) shouldBe expected
  }

  "Given 2 points" should "return matched route" taggedAs Slow in {

    val point1 = GPSPoint(45.16696, 8.89223, None, 1619275184000L)
    val point2 = GPSPoint(45.16696, 8.89223, None, 1619275191000L)

    val points = Seq(point1, point2)

    val response = manager.matchingRoute(points).right.value

    // FIXME road type should be residential https://nominatim.openstreetmap.org/ui/details.html?osmtype=W&osmid=221099743&class=highway
    val expected = MatchedRoute(
      List(
        TracePoint(
          latitude = 45.16696,
          longitude = 8.89223,
          altitude = None,
          time = 1619275184000L,
          matchedLatitude = Some(45.16695452670932),
          matchedLongitude = Some(8.892203898204867),
          matchedAltitude = Some(0.0),
          roadType = Some("service"),
          roadName = Some(""),
          speedLimit = Some(20),
          linearDistance = Some(2.13489478036631)
        ),
        TracePoint(
          latitude = 45.16696,
          longitude = 8.89223,
          altitude = None,
          time = 1619275191000L,
          matchedLatitude = Some(45.16695452670932),
          matchedLongitude = Some(8.892203898204867),
          matchedAltitude = Some(0.0),
          roadType = Some("service"),
          roadName = Some(""),
          speedLimit = Some(20),
          linearDistance = Some(2.13489478036631)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("service" -> 65.105),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  "Given route of a stopped vehicle" should "return a matched route" taggedAs Slow in {

    val trip =
      """
        |45.17264,9.04017,1619089290000
        |45.17264,9.04016,1619089300000
        |45.17264,9.04016,1619089310000
        |45.17264,9.04016,1619089320000
        |45.17264,9.04016,1619089330000
        |45.17264,9.04016,1619089340000
        |45.17264,9.04016,1619089350000
        |45.17264,9.04016,1619089360000
        |45.17264,9.04016,1619089370000
        |45.17264,9.04016,1619089380000
        |45.17264,9.04016,1619089390000
        |45.17264,9.04016,1619089400000
        |45.17264,9.04016,1619089410000
        |45.17262,9.04016,1619089411000
        |""".stripMargin

    val points: Seq[GPSPoint] = trip.lines
      .filter(_.nonEmpty)
      .map(_.split(","))
      .map(r => GPSPoint(r.head.toDouble, r(1).toDouble, None, r(2).toLong))
      .toSeq

    val response = manager.matchingRoute(points).right.value

    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=N&osmid=4332601289&class=amenity
    val expected = MatchedRoute(
      points = List(
        TracePoint(
          latitude = 45.17264,
          longitude = 9.04017,
          altitude = None,
          time = 1619089290000L,
          matchedLatitude = Some(45.17239819165617),
          matchedLongitude = Some(9.040047061706336),
          matchedAltitude = Some(0.0),
          roadType = Some("residential"),
          roadName = Some("Via Stefano Pollini"),
          speedLimit = Some(30),
          linearDistance = Some(28.562745223944667)
        ),
        TracePoint(
          latitude = 45.17262,
          longitude = 9.04016,
          altitude = None,
          time = 1619089411000L,
          matchedLatitude = Some(45.17239815398451),
          matchedLongitude = Some(9.04004721079609),
          matchedAltitude = Some(0.0),
          roadType = Some("residential"),
          roadName = Some("Via Stefano Pollini"),
          speedLimit = Some(30),
          linearDistance = Some(26.20476715546049)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("residential" -> 159.296),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  "test carFlagEncoderEnrich" should "retrieve result of map matching and distance for each type of street" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(45.074246, 7.642711, None, 1552910827000L),
      GPSPoint(45.075511, 7.643988, None, 1552910928000L),
      GPSPoint(45.075511, 7.643988, None, 1552910929000L),
      GPSPoint(45.075511, 7.643988, None, 1552910930000L)
    )

    val response = manager.matchingRoute(gpsPoint).right.value

    assert(response.length > 150 && response.length < 200)
    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=N&osmid=332648726&class=railway
    assert(response.getKmType("primary").isSuccess)
    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=N&osmid=4480107446&class=place
    assert(response.getKmType("residential").isSuccess)
  }

  "test with point near sea" should "exclude ferries" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(38.12, 13.37, None, 1552910827000L),
      GPSPoint(38.12, 13.367, None, 1552910928000L),
      GPSPoint(38.1303, 13.3645, None, 1552910929000L),
      GPSPoint(38.1302, 13.3644, None, 1552910930000L)
    )

    val response = manager.matchingRoute(gpsPoint).right.value

    assert(response.length > 1700 && response.length < 3200)
    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=W&osmid=176323423&class=highway
    assert(response.getKmType("service").isSuccess)
    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=N&osmid=3385101726&class=amenity
    assert(response.getKmType("secondary").isSuccess)
  }

  "test with point near sea or in sea" should "exclude ferries" taggedAs Slow in {

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

    val response = manager.matchingRoute(gpsPoint).right.value

    assert(response.length > 10500 && response.length < 11500)
  }

  "test with point near pedestrian area" should "exclude pedestrian area" taggedAs Slow in {

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

  "test with type road is null" should "change in unclassified" taggedAs Slow in {

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

  "trip" should "retrieve points sorted by timestamp" taggedAs Slow in {

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

  "test with point to calculate distance " should "calculate distance between points" taggedAs Slow in {

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

  private def noneAltitude(r: MatchedRoute): MatchedRoute =
    r.copy(points = r.points.map(_.copy(altitude = None))) // Altitude might be Some(NaN)

  it should "match route motorway road type in Rome" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(41.511383, 13.650297, None, 1568268784000L),
      GPSPoint(41.511383, 13.650297, None, 1568268786000L)
    )

    val response = manager.matchingRoute(gpsPoint).right.value

    val expected = MatchedRoute(
      points = List(
        TracePoint(
          latitude = 41.511383,
          longitude = 13.650297,
          altitude = None,
          time = 1568268784000L,
          matchedLatitude = Some(41.51142257124358),
          matchedLongitude = Some(13.650327262988895),
          matchedAltitude = Some(0.0),
          roadType = Some("motorway"),
          roadName = Some("Autostrada del Sole, A1"),
          speedLimit = Some(130),
          linearDistance = Some(5.070578365616436)
        ),
        TracePoint(
          latitude = 41.511383,
          longitude = 13.650297,
          altitude = None,
          time = 1568268786000L,
          matchedLatitude = Some(41.51142257124358),
          matchedLongitude = Some(13.650327262988895),
          matchedAltitude = Some(0.0),
          roadType = Some("motorway"),
          roadName = Some("Autostrada del Sole, A1"),
          speedLimit = Some(130),
          linearDistance = Some(5.070578365616436)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("motorway" -> 4534.341),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  it should "match route road type residential in Rome" taggedAs Slow in {
    val gpsPoint: List[GPSPoint] = List(GPSPoint(41.79416, 12.43534, None, 1568268784000L))

    val response = manager.matchingRoute(gpsPoint).right.value

    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=N&osmid=1762469004&class=highway
    val expected = MatchedRoute(
      points = List(
        TracePoint(
          latitude = 41.79416,
          longitude = 12.43534,
          altitude = None,
          time = 1568268784000L,
          matchedLatitude = Some(41.79415698265517),
          matchedLongitude = Some(12.435335536254428),
          matchedAltitude = Some(0.0),
          roadType = Some("residential"),
          roadName = Some("Via Armando Brasini"),
          speedLimit = Some(50),
          linearDistance = Some(0.4995042875174139)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("residential" -> 175.438),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  it should "match route road type motorway in Rome" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(GPSPoint(41.82721, 12.71078, None, 1568268784000L))

    val response = manager.matchingRoute(gpsPoint).right.value

    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=W&osmid=330339383&class=highway
    val expected = MatchedRoute(
      points = List(
        TracePoint(
          latitude = 41.82721,
          longitude = 12.71078,
          altitude = None,
          time = 1568268784000L,
          matchedLatitude = Some(41.827229072913404),
          matchedLongitude = Some(12.710784455770657),
          matchedAltitude = Some(0.0),
          roadType = Some("motorway"),
          roadName = Some("Diramazione Roma Sud, A1"),
          speedLimit = Some(130),
          linearDistance = Some(2.15270662458055)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("motorway" -> 189.24),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  it should "match route road type motorway in Rome with max speed enrichment" taggedAs Slow in {
    val gpsPoint: List[GPSPoint] = List(GPSPoint(41.82244, 12.75443, None, 1568268784000L))

    val response = manager.matchingRoute(gpsPoint).right.value

    // https://nominatim.openstreetmap.org/ui/details.html?osmtype=W&osmid=13276603&class=highway
    val expected = MatchedRoute(
      points = List(
        TracePoint(
          latitude = 41.82244,
          longitude = 12.75443,
          altitude = None,
          time = 1568268784000L,
          matchedLatitude = Some(41.82246520227082),
          matchedLongitude = Some(12.754441399762891),
          matchedAltitude = Some(0.0),
          roadType = Some("motorway"),
          roadName = Some("Diramazione Roma Sud, A1"),
          speedLimit = Some(100),
          linearDistance = Some(2.9572919270786806)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("motorway" -> 1562.871),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  it should "match route road type secondary in Rome" taggedAs Slow in {
    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(41.82716, 12.70839, None, 1568268784000L)
    )

    val response = manager.matchingRoute(gpsPoint).right.value

    val expected = MatchedRoute(
      points = List(
        TracePoint(
          latitude = 41.82716,
          longitude = 12.70839,
          altitude = None,
          time = 1568268784000L,
          matchedLatitude = Some(41.82715787130136),
          matchedLongitude = Some(12.708415820027746),
          matchedAltitude = Some(0.0),
          roadType = Some("secondary"),
          roadName = Some("Via Fontana Candida"),
          speedLimit = Some(60),
          linearDistance = Some(2.1524503899857126)
        )
      ),
      length = 0.0,
      time = 0,
      routes = Map("secondary" -> 89.541),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }
}
