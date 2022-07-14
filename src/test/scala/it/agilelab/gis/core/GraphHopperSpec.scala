package it.agilelab.gis.core

import com.typesafe.config.{ Config, ConfigFactory }
import it.agilelab.gis.core.apps.ConverterFromOSMToGraphHopperMap
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.graphhopper._
import org.scalatest._
import org.scalatest.tagobjects.Slow

import java.io.File
import java.nio.file.{ Files, Paths }

/** @author andreaL
  */
class GraphHopperSpec
    extends FlatSpec
    with Matchers
    with EitherValues
    with TryValues
    with OptionValues
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
      length = Some(0.0),
      time = Some(0),
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

    val tp1 = TracePoint(
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
    )

    val tp2 = TracePoint(
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

    val expected = MatchedRoute(
      points = Seq(tp1, tp2),
      length = Some(38.44312467891209),
      time = Some(1729),
      routes = Map("motorway" -> 901.385),
      distanceBetweenPoints = Seq(
        DistancePoint(
          tp1,
          tp2,
          distance = Some(38.44312467891209),
          diffTime = 7000L,
          Some("motorway")
        )
      )
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
      length = Some(0.0),
      time = Some(0),
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
    val tp1 = TracePoint(
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
    )

    val tp2 = TracePoint(
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

    val expected = MatchedRoute(
      List(tp1, tp2),
      length = Some(0.0),
      time = Some(0),
      routes = Map("service" -> 65.105),
      distanceBetweenPoints = List(DistancePoint(tp1, tp2, Some(0), tp2.time - tp1.time, None))
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
    val tp1 = TracePoint(
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
    )

    val tp2 = TracePoint(
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

    val expected = MatchedRoute(
      points = List(tp1, tp2),
      length = Some(0.0),
      time = Some(0),
      routes = Map("residential" -> 159.296),
      distanceBetweenPoints = List(DistancePoint(tp1, tp2, Some(0), tp2.time - tp1.time, None))
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

    assert(response.length.value > 150 && response.length.value < 200)
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

    assert(response.length.value > 1700 && response.length.value < 3200)
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

    assert(response.length.value > 10500 && response.length.value < 11500)
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

    assert(matchingRoute.length.value > 1500 && matchingRoute.length.value < 2500)
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

    val response = manager.matchingRoute(gpsPoint).right.value

    distanceBetween(response, 1500d, 1800d)
  }

  "test with point to calculate distance " should "calculate distance between points" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(42.4599, 12.3813, None, 1568268784000L),
      GPSPoint(42.46, 12.3823, None, 1568268786000L)
    )

    val matchingRoute = manager.matchingRoute(gpsPoint).right.value

    matchingRoute.points should have length 2
    matchingRoute.distanceBetweenPoints should have length 1

    val distanceBetweenPoints = matchingRoute.distanceBetweenPoints.head

    assert(distanceBetweenPoints.diffTime == 2000L)
    assert(distanceBetweenPoints.distance.get > 75 && distanceBetweenPoints.distance.get < 85)
  }

  private def noneAltitude(r: MatchedRoute): MatchedRoute =
    r.copy(
      points = r.points.map(_.copy(altitude = None)),
      distanceBetweenPoints = r.distanceBetweenPoints.map(d =>
        d.copy(node1 = d.node1.copy(altitude = None), node2 = d.node2.copy(altitude = None)))
    ) // Altitude might be Some(NaN)

  it should "match route motorway road type in Rome" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(41.511383, 13.650297, None, 1568268784000L),
      GPSPoint(41.511383, 13.650297, None, 1568268786000L)
    )

    val response = manager.matchingRoute(gpsPoint).right.value

    val tp1 = TracePoint(
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
    )

    val tp2 = TracePoint(
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

    val expected = MatchedRoute(
      points = List(tp1, tp2),
      length = Some(0.0),
      time = Some(0),
      routes = Map("motorway" -> 4534.341),
      distanceBetweenPoints = List(DistancePoint(tp1, tp2, Some(0), tp2.time - tp1.time, None))
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
      length = Some(0.0),
      time = Some(0),
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
      length = Some(0.0),
      time = Some(0),
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
      length = Some(0.0),
      time = Some(0),
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
      length = Some(0.0),
      time = Some(0),
      routes = Map("secondary" -> 89.541),
      distanceBetweenPoints = List()
    )

    noneAltitude(response) shouldBe expected
  }

  it should "perform length" taggedAs Slow in {
    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(42.46046266264412, 12.379277358099305, None, 1552910827000L),
      GPSPoint(42.46050866930525, 12.380000883981737, None, 1552910928000L),
      GPSPoint(42.45108805719714, 12.372518094875302, None, 1552910928000L),
      GPSPoint(42.43274662475259, 12.366294417086316, None, 1552910928000L)
    )

    val response = manager.matchingRoute(gpsPoint).right.value

    response.points.length should be (response.distanceBetweenPoints.length + 1)

    distanceBetween(response, 4000d, 4500d)
  }

  it should "return empty distance between points on a trip with a stopped vehicle and unmatchable route" taggedAs Slow in {

    val points = """
      |40.74461 14.4759
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47612
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |40.74456 14.47613
      |""".stripMargin

    val gpsPoints = points.lines
      .filter(_.nonEmpty)
      .map(p => p.split(" "))
      .zipWithIndex
      .map { case (p, idx) => GPSPoint(p(0).toDouble, p(1).toDouble, None, idx.toLong) }
      .toList

    val result = manager.matchingRoute(gpsPoints).right.value

    result.distanceBetweenPoints should have length 0
  }

  it should "return distance between points on a trip with a stopped vehicle and matchable route" taggedAs Slow in {
    val points = """
                   |40.7445 14.47465
                   |40.7445 14.47465
                   |40.7445 14.47465
                   |""".stripMargin

    val gpsPoints = points.lines
      .filter(_.nonEmpty)
      .map(p => p.split(" "))
      .zipWithIndex
      .map { case (p, idx) => new GPSPoint(p(0).toDouble, p(1).toDouble, None, idx.toLong) }
      .toList

    val result = manager.matchingRoute(gpsPoints).right.value

    result.distanceBetweenPoints.length should be (result.points.length - 1)
  }

  it should "return distance between points on a matchable route" taggedAs Slow in { // DONE
    val points = """
        |43.67814 12.38891
        |43.6772 12.38942
        |43.6767 12.39109
        |43.67664 12.39244
        |43.6766 12.39371
        |43.67613 12.39484
        |43.67554 12.39515
        |""".stripMargin

    val gpsPoints = points.lines
      .filter(_.nonEmpty)
      .map(p => p.split(" "))
      .zipWithIndex
      .map { case (p, idx) => new GPSPoint(p(0).toDouble, p(1).toDouble, None, idx.toLong) }
      .toList

    val result = manager.matchingRoute(gpsPoints).right.value

    distanceBetween(result, 990d, 1000d)
  }

  "test carFlagEncoderEnrich 2" should "retrieve result of map matching and distance for each type of street" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(43.67814, 12.38891, None, 1),
      GPSPoint(43.6772, 12.38942, None, 2),
      GPSPoint(43.6767, 12.39109, None, 3),
      GPSPoint(43.67664, 12.39244, None, 4),
      GPSPoint(43.6766, 12.39371, None, 5),
      GPSPoint(43.67613, 12.39484, None, 6),
      GPSPoint(43.67554, 12.39515, None, 7)
    )

    val response: MatchedRoute = manager.matchingRoute(gpsPoint).right.value

    distanceBetween(response, 990d, 995d)
  }

  "test carFlagEncoderEnrich 3" should "retrieve result of map matching and distance for each type of street" taggedAs Slow in {

    val gpsPoint: List[GPSPoint] = List(
      GPSPoint(45.07248552073306, 7.56254494706547, None, 1),
      GPSPoint(45.07527372572808, 7.629149561437075, None, 2),
      GPSPoint(45.06945470883816, 7.6597052865869335, None, 3),
      GPSPoint(45.065090057355704, 7.672579889880414, None, 4)
    )

    val response: MatchedRoute = manager.matchingRoute(gpsPoint).right.value

    distanceBetween(response, 8900d, 9000d)
  }

  "test carFlagEncoderEnrich 4" should "retrieve result of map matching and distance for each type of street" taggedAs Slow in {

    val points = """
      |45.75124, 45.7513, 45.75139, 45.75133, 45.75101, 45.75068, 45.75014, 45.74992, 45.75026, 45.75116, 45.7523, 45.75354, 45.75482, 45.75606, 45.75635, 45.75644, 45.75654, 45.75663, 45.75772, 45.75865, 45.75876, 45.7588, 45.75886, 45.75892, 45.75897, 45.75936, 45.76017, 45.7611, 45.76208, 45.763, 45.76391, 45.76518, 45.76633, 45.76753, 45.76785, 45.76797, 45.76908, 45.76927, 45.7692, 45.7692, 45.7692, 45.7692, 45.76919, 45.76918, 45.76918, 45.76918, 45.76916, 45.76916, 45.76916, 45.76917, 45.76918
      |9.05274, 9.05281, 9.05241, 9.05231, 9.05236, 9.05189, 9.05106, 9.05045, 9.05038, 9.05043, 9.05048, 9.05055, 9.05066, 9.05119, 9.05154, 9.0516, 9.05161, 9.05159, 9.05149, 9.05191, 9.05215, 9.05221, 9.05224, 9.05223, 9.0522, 9.05194, 9.05212, 9.05317, 9.05438, 9.05551, 9.05662, 9.05756, 9.05836, 9.05942, 9.05971, 9.05977, 9.06032, 9.06098, 9.06106, 9.06106, 9.06106, 9.06106, 9.06107, 9.06107, 9.06107, 9.06107, 9.06106, 9.06106, 9.06106, 9.06106, 9.06106
      |""".stripMargin.lines
      .filter(_.nonEmpty)
      .map(l => l.split(","))
      .map(l => l.filter(_.nonEmpty).map(_.trim.toDouble))
      .toList

    points should have length 2

    val lats = points.head
    val lots = points.last

    lats.length should be (lots.length)

    val gpsPoints = lats.zip(lots).zipWithIndex.map { case ((lat, lon), idx) => GPSPoint(lat, lon, None, idx.toLong) }

    val response: MatchedRoute = manager.matchingRoute(gpsPoints).right.value

    distanceBetween(response, 2680d, 2700d)
  }

  it should "perform distance between each matched point accurately" taggedAs Slow in {
    val points =
      """
        |  41.78176 12.33408
        |  41.78199 12.33372
        |  41.78269 12.33316
        |  41.78338 12.33262
        |  41.78395 12.33219
        |  41.78445 12.33178
        |   41.7849 12.33144
        |  41.78513 12.33126
        |  41.78576 12.33074
        |  41.78617 12.33044
        |  41.78645 12.33021
        |  41.78638  12.3299
        |  41.78617 12.32947
        |  41.78603 12.32914
        |  41.78606 12.32908
        |  41.78607 12.32908
        |  41.78605 12.32912
        |  41.78606 12.32914
        |  41.78606 12.32917
        |  41.78623 12.32961
        |  41.78642 12.33003
        |  41.78646 12.33014
        |  41.78628 12.33036
        |  41.78563 12.33086
        |  41.78503 12.33132
        |  41.78446 12.33178
        |  41.78374 12.33235
        |  41.78297 12.33294
        |  41.78221 12.33355
        |   41.7815 12.33409
        |  41.78079 12.33463
        |     41.78 12.33527
        |  41.77918 12.33592
        |  41.77841 12.33653
        |   41.7781 12.33705
        |  41.77801 12.33762
        |  41.77829 12.33844
        |  41.77856 12.33926
        |  41.77865 12.33957
        |  41.77836 12.33981
        |  41.77775 12.34016
        |  41.77736  12.3404
        |  41.77735  12.3404
        |  41.77735  12.3404
        |  41.77732 12.34043
        |  41.77732 12.34041
        |  41.77731 12.34042
        |  41.77706  12.3406
        |   41.7766 12.34093
        |  41.77653 12.34096
        |  41.77635 12.34106
        |  41.77618 12.34122
        |  41.77638 12.34171
        |  41.77662 12.34233
        |  41.77685 12.34301
        |  41.77724 12.34401
        |  41.77768 12.34515
        |  41.77813 12.34638
        |  41.77863 12.34762
        |  41.77907  12.3488
        |  41.77946 12.34989
        |  41.77989 12.35104
        |  41.78033 12.35219
        |  41.78085 12.35353
        |  41.78137 12.35479
        |  41.78198 12.35598
        |   41.7825 12.35698
        |  41.78273 12.35794
        |  41.78327 12.35902
        |  41.78371 12.36003
        |  41.78394  12.3607
        |  41.78418 12.36144
        |  41.78443 12.36228
        |  41.78492 12.36379
        |  41.78542  12.3654
        |  41.78594  12.3666
        |  41.78668 12.36791
        |  41.78734 12.36971
        |  41.78799 12.37159
        |   41.7887 12.37354
        |  41.78936 12.37541
        |  41.78995 12.37701
        |  41.79057 12.37871
        |  41.79126 12.38067
        |  41.79175 12.38275
        |  41.79215 12.38473
        |  41.79253 12.38659
        |  41.79287 12.38841
        |  41.79323 12.39019
        |  41.79357 12.39192
        |  41.79392 12.39365
        |   41.7943 12.39554
        |   41.7947 12.39751
        |  41.79508 12.39944
        |  41.79544 12.40134
        |  41.79583 12.40329
        |  41.79617 12.40527
        |  41.79644 12.40724
        |  41.79674 12.40923
        |   41.7971 12.41125
        |  41.79772 12.41314
        |   41.7983 12.41492
        |  41.79918  12.4165
        |  41.80054 12.41757
        |  41.80238 12.41857
        |  41.80343 12.41911
        |  41.80484  12.4197
        |  41.80649 12.42014
        |  41.80809  12.4209
        |  41.80927 12.42232
        |  41.81033 12.42379
        |  41.81095 12.42336
        |  41.81023 12.42215
        |   41.8093  12.4211
        |  41.80891 12.42078
        |  41.80828 12.42035
        |    41.808 12.41927
        |  41.80826 12.41738
        |  41.80856 12.41531
        |  41.80883 12.41309
        |  41.80896 12.41112
        |  41.80921 12.40888
        |  41.80985 12.40541
        |  41.81068 12.40182
        |  41.81198 12.39823
        |  41.81375 12.39524
        |  41.81561 12.39307
        |  41.81745 12.39161
        |  41.81948 12.39053
        |  41.82216  12.3895
        |  41.82514 12.38855
        |  41.82829 12.38756
        |  41.83143 12.38665
        |  41.83449 12.38576
        |  41.83765 12.38476
        |  41.84066  12.3837
        |  41.84366 12.38255
        |  41.84657 12.38136
        |  41.84922 12.38019
        |  41.85193 12.37892
        |  41.85495 12.37767
        |  41.85774 12.37648
        |  41.86059  12.3753
        |  41.86341 12.37417
        |  41.86576  12.3739
        |  41.86774 12.37432
        |  41.86944 12.37515
        |  41.87095 12.37598
        |  41.87355 12.37731
        |  41.87649 12.37887
        |  41.88011 12.38053
        |  41.88385 12.38095
        |  41.88759 12.38106
        |  41.89073 12.38123
        |   41.8937  12.3815
        |  41.89663 12.38179
        |  41.89907  12.3827
        |  41.90081 12.38341
        |  41.90226 12.38396
        |  41.90355 12.38436
        |  41.90386 12.38519
        |  41.90381 12.38606
        |  41.90405 12.38698
        |  41.90482 12.38802
        |  41.90516 12.38941
        |    41.905 12.39052
        |  41.90484 12.39049
        |   41.9048 12.39043
        |   41.9048 12.39043
        |   41.9048 12.39043
        |   41.9048 12.39043
        |   41.9048 12.39043
        |   41.9048 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39043
        |  41.90479 12.39042
        |  41.90479 12.39042
        |  41.90479 12.39043
        |   41.9048 12.39045
        |  41.90482 12.39049
        |  41.90482 12.39049
        |  41.90488 12.39038
        |  41.90491 12.39038
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |  41.90494 12.39043
        |
        |""".stripMargin

    val gpsPoints = points.lines
      .filter(_.nonEmpty)
      .map(p => p.trim.split(" ").filter(_.trim.nonEmpty))
      .zipWithIndex
      .map { case (p, idx) => new GPSPoint(p(0).toDouble, p(1).toDouble, None, idx.toLong) }
      .toList

    val result = manager.matchingRoute(gpsPoints).right.value

    result.distanceBetweenPoints.length should be (result.points.length - 1)

    distanceBetween(result, 24900d, 24950d)

    result.distanceBetweenPoints.head.distance shouldBe None
    result.distanceBetweenPoints(1).distance shouldBe None
    result.distanceBetweenPoints(2).distance shouldBe None
    distanceBetween(result.distanceBetweenPoints(3), 515d, 525d)
  }

  it should "perform distance between points accurately - acerra" taggedAs Slow in {

    val points = csvToPoints(
      """
        |"0",40.94563,14.35663,1624220598000
        |"10",40.94568,14.35664,1624220609000
        |"20",40.9457,14.35658,1624220619000
        |"30",40.94569,14.35645,1624220629000
        |"40",40.94573,14.35645,1624220639000
        |"50",40.94569,14.35641,1624220649000
        |"60",40.94567,14.3564,1624220659000
        |"70",40.94597,14.35655,1624220669000
        |"80",40.94617,14.35672,1624220679000
        |"90",40.94603,14.35753,1624220689000
        |"100",40.94598,14.35836,1624220699000
        |"110",40.94663,14.35856,1624220709000
        |"120",40.94694,14.35891,1624220719000
        |"130",40.9464,14.35987,1624220729000
        |"140",40.94575,14.3613,1624220739000
        |"150",40.94528,14.36274,1624220749000
        |"160",40.94493,14.36299,1624220759000
        |"170",40.94464,14.36275,1624220769000
        |"180",40.94386,14.3628,1624220779000
        |"190",40.94376,14.3633,1624220789000
        |"200",40.9434,14.3636,1624220799000
        |"210",40.94312,14.36328,1624220809000
        |"220",40.94306,14.36277,1624220819000
        |"230",40.94297,14.36278,1624220829000
        |"240",40.94302,14.36272,1624220839000
        |"250",40.94295,14.36271,1624220849000
        |"260",40.943,14.36272,1624220859000
        |"270",40.943,14.36273,1624220869000
        |"280",40.943,14.36267,1624220879000
        |"290",40.94301,14.3627,1624220889000
        |"300",40.94305,14.36271,1624220899000
        |"310",40.94295,14.36267,1624220909000
        |"320",40.9431,14.36282,1624220919000
        |"""
    )

    val result = manager.matchingRoute(points).right.value

    result.distanceBetweenPoints.length should be (result.points.length - 1)

    distanceBetween(result, 1050, 1150d)

    distanceBetween(result.distanceBetweenPoints.head, 150d, 170d)
    distanceBetween(result.distanceBetweenPoints(1), 130d, 140d)
    distanceBetween(result.distanceBetweenPoints(2), 100d, 110d)
    result.distanceBetweenPoints(3).distance shouldBe None
    distanceBetween(result.distanceBetweenPoints(4), 270d, 280d)
    distanceBetween(result.distanceBetweenPoints(5), 200d, 210d)
    result.distanceBetweenPoints(6).distance shouldBe None
    distanceBetween(result.distanceBetweenPoints(7), 200d, 210d)
  }

  it should "perform distance between points accurately - bagnolo" taggedAs Slow in {

    val points = csvToPoints(
      """
        |"0",45.42325,10.19158,1624213195000
        |"10",45.42398,10.19118,1624213205000
        |"20",45.42496,10.19058,1624213215000
        |"30",45.42612,10.18987,1624213225000
        |"40",45.42689,10.18919,1624213235000
        |"50",45.42766,10.18864,1624213245000
        |"60",45.42849,10.18798,1624213255000
        |"70",45.42917,10.18814,1624213265000
        |"80",45.43031,10.1889,1624213275000
        |"90",45.43158,10.18974,1624213285000
        |"100",45.4329,10.19034,1624213295000
        |"110",45.43424,10.19019,1624213305000
        |"120",45.43536,10.18999,1624213315000
        |"130",45.43514,10.19041,1624213325000
        |"140",45.43509,10.19135,1624213335000
        |"150",45.43505,10.19318,1624213345000
        |"160",45.43519,10.19507,1624213355000
        |"170",45.43506,10.19671,1624213365000
        |"180",45.4348,10.19749,1624213375000
        |"190",45.43412,10.19713,1624213385000
        |"200",45.43392,10.19784,1624213395000
        |"210",45.43379,10.19853,1624213405000
        |"220",45.43389,10.19863,1624213415000
        |"230",45.43402,10.19868,1624213425000
        |"240",45.43403,10.19865,1624213435000
        |"250",45.43403,10.19864,1624213445000
        |"260",45.43403,10.19863,1624213455000
        |"270",45.43402,10.19863,1624213465000
        |"280",45.43402,10.19864,1624213475000
        |"290",45.43401,10.19865,1624213485000
        |"300",45.43402,10.19862,1624213495000
        |"310",45.43403,10.19861,1624213505000
        |"320",45.43404,10.19861,1624213515000
        |"330",45.43404,10.19861,1624213525000
        |"340",45.43404,10.1986,1624213535000
        |"350",45.43405,10.1986,1624213545000
        |"360",45.43405,10.19858,1624213555000
        |"370",45.43405,10.19859,1624213565000
        |"380",45.43405,10.19859,1624213575000
        |"390",45.43405,10.19859,1624213585000
        |"400",45.43405,10.19859,1624213595000
        |"410",45.43405,10.19859,1624213605000
        |"420",45.43405,10.1986,1624213615000
        |"430",45.43406,10.1986,1624213625000
        |"440",45.43405,10.1986,1624213635000
        |"450",45.43405,10.1986,1624213645000
        |"460",45.43407,10.19859,1624213655000
        |"470",45.43406,10.1986,1624213665000
        |"480",45.43407,10.19859,1624213675000
        |"490",45.43408,10.19858,1624213685000
        |"500",45.43407,10.19859,1624213695000
        |"510",45.43407,10.19859,1624213705000
        |"520",45.43408,10.19858,1624213715000
        |"530",45.43407,10.19858,1624213725000
        |"540",45.43408,10.19857,1624213735000
        |"550",45.43408,10.19857,1624213745000
        |""".stripMargin
    )

    val result = manager.matchingRoute(points).right.value

    result.distanceBetweenPoints.length should be (result.points.length - 1)

    distanceBetween(result, 2300d, 2500d)

    distanceBetween(result.distanceBetweenPoints.head, 200d, 230d)
    distanceBetween(result.distanceBetweenPoints(1), 135d, 145d)
    distanceBetween(result.distanceBetweenPoints(2), 100d, 110d)
    distanceBetween(result.distanceBetweenPoints(3), 200d, 210d)
    distanceBetween(result.distanceBetweenPoints(4), 215d, 225d)
    distanceBetween(result.distanceBetweenPoints(5), 150d, 160d)
    distanceBetween(result.distanceBetweenPoints(6), 150d, 160d)
    distanceBetween(result.distanceBetweenPoints(7), 170d, 180d)
    distanceBetween(result.distanceBetweenPoints(8), 135d, 145d)
    // ...
  }

  it should "perform distance between points accurately - cittadella" taggedAs Slow in {
    val points = csvToPoints(
      """
        |"0",45.60334,11.80602,1622738364000
        |"10",45.60325,11.80583,1622738375000
        |"20",45.60326,11.80581,1622738385000
        |"30",45.60324,11.80578,1622738395000
        |"40",45.60311,11.80578,1622738405000
        |"50",45.60306,11.80578,1622738415000
        |"60",45.60304,11.80581,1622738425000
        |"70",45.60304,11.80585,1622738435000
        |"80",45.60303,11.80587,1622738445000
        |"90",45.60303,11.80587,1622738455000
        |"100",45.60303,11.80587,1622738465000
        |"110",45.60295,11.80553,1622738475000
        |"120",45.60269,11.80485,1622738485000
        |"130",45.6022,11.80515,1622738495000
        |"140",45.60144,11.80563,1622738505000
        |"150",45.60108,11.80498,1622738515000
        |"160",45.60066,11.80341,1622738525000
        |"170",45.6007,11.80252,1622738535000
        |"180",45.60149,11.80225,1622738545000
        |"190",45.60266,11.80181,1622738555000
        |"200",45.60359,11.80142,1622738565000
        |"210",45.6046,11.80098,1622738575000
        |"220",45.60517,11.80075,1622738585000
        |"230",45.60591,11.80044,1622738595000
        |"240",45.60675,11.80009,1622738605000
        |"250",45.60788,11.79962,1622738615000
        |"260",45.60918,11.79911,1622738625000
        |"270",45.61056,11.79864,1622738635000
        |"280",45.61184,11.79818,1622738645000
        |"290",45.61299,11.79772,1622738655000
        |"300",45.61413,11.79728,1622738665000
        |"310",45.61514,11.7969,1622738675000
        |"320",45.61574,11.79666,1622738685000
        |"330",45.6162,11.79646,1622738695000
        |"340",45.61653,11.79633,1622738705000
        |"350",45.61675,11.79624,1622738715000
        |"360",45.61701,11.79615,1622738725000
        |"370",45.61738,11.79588,1622738735000
        |"380",45.61724,11.79467,1622738745000
        |"390",45.61686,11.79285,1622738755000
        |"400",45.6165,11.79101,1622738765000
        |"410",45.61611,11.789,1622738775000
        |"420",45.61569,11.78685,1622738785000
        |"430",45.61525,11.78467,1622738795000
        |"440",45.61478,11.78239,1622738805000
        |"450",45.61433,11.78013,1622738815000
        |"460",45.61391,11.778,1622738825000
        |"470",45.61349,11.77584,1622738835000
        |"480",45.61308,11.77377,1622738845000
        |"490",45.61268,11.7718,1622738855000
        |"500",45.61234,11.77005,1622738865000
        |"510",45.61232,11.76864,1622738875000
        |"520",45.61328,11.76787,1622738885000
        |"530",45.61403,11.76749,1622738895000
        |"540",45.61383,11.76632,1622738905000
        |"550",45.61336,11.7652,1622738915000
        |"560",45.61251,11.7644,1622738925000
        |"570",45.6118,11.76366,1622738935000
        |"580",45.61084,11.76293,1622738945000
        |"590",45.61047,11.76248,1622738955000
        |"600",45.61087,11.76209,1622738965000
        |"610",45.6103,11.76103,1622738975000
        |"620",45.61003,11.76048,1622738985000
        |"630",45.61007,11.75985,1622738995000
        |"640",45.6104,11.75986,1622739005000
        |"650",45.61037,11.75994,1622739015000
        |"660",45.61036,11.76002,1622739025000
        |"670",45.61036,11.76002,1622739035000
        |"680",45.61036,11.76002,1622739045000
        |"690",45.61036,11.76002,1622739055000
        |"700",45.61036,11.76002,1622739065000
        |"710",45.61036,11.76002,1622739075000
        |"720",45.61036,11.76003,1622739085000
        |"730",45.61036,11.76003,1622739095000
        |"740",45.61036,11.76003,1622739105000
        |"750",45.61036,11.76002,1622739115000
        |"760",45.61036,11.76002,1622739125000
        |"770",45.61036,11.76002,1622739135000
        |"780",45.61036,11.76001,1622739145000
        |"790",45.61036,11.76002,1622739155000
        |"800",45.61036,11.76001,1622739165000
        |"810",45.61036,11.76001,1622739175000
        |"820",45.61036,11.76001,1622739185000
        |"830",45.61036,11.76001,1622739195000
        |"840",45.61036,11.76001,1622739205000
        |"850",45.61035,11.76001,1622739215000
        |"860",45.61036,11.76001,1622739225000
        |"870",45.61036,11.76001,1622739235000
        |"880",45.61035,11.76001,1622739245000
        |"890",45.61035,11.76001,1622739255000
        |"900",45.61035,11.76001,1622739265000
        |"910",45.61035,11.76001,1622739275000
        |"920",45.61035,11.76001,1622739285000
        |"930",45.61035,11.76001,1622739295000
        |"940",45.61035,11.76001,1622739305000
        |"950",45.61035,11.76001,1622739315000
        |"960",45.61035,11.76001,1622739325000
        |"970",45.61035,11.76001,1622739335000
        |"980",45.61035,11.76001,1622739345000
        |"990",45.61035,11.76001,1622739355000
        |"1000",45.61035,11.76001,1622739365000
        |"1010",45.61035,11.76001,1622739375000
        |"1020",45.61035,11.76001,1622739385000
        |"1030",45.61035,11.76001,1622739395000
        |"1040",45.61035,11.76001,1622739405000
        |"1050",45.61035,11.76001,1622739415000
        |"1060",45.61035,11.76001,1622739425000
        |"1070",45.61035,11.76001,1622739435000
        |"1080",45.61035,11.76001,1622739445000
        |"1090",45.61035,11.76001,1622739455000
        |"1100",45.61035,11.76001,1622739465000
        |"1110",45.61035,11.76,1622739475000
        |"1120",45.61035,11.76,1622739485000
        |"1130",45.61035,11.76,1622739495000
        |"1140",45.61035,11.76,1622739505000
        |"1150",45.61035,11.76,1622739515000
        |"1160",45.61035,11.76,1622739525000
        |"1170",45.61035,11.76,1622739535000
        |"1180",45.61035,11.76,1622739545000
        |"1190",45.61035,11.76,1622739555000
        |"1200",45.61035,11.75999,1622739565000
        |"1210",45.61035,11.75999,1622739575000
        |"1220",45.61035,11.75999,1622739585000
        |"1230",45.61035,11.75981,1622739595000
        |"1240",45.61009,11.75992,1622739605000
        |"1250",45.61006,11.76059,1622739615000
        |"1260",45.61034,11.7611,1622739625000
        |"1270",45.61091,11.76214,1622739635000
        |"1280",45.61049,11.7625,1622739645000
        |"1290",45.61106,11.76313,1622739655000
        |"1300",45.61194,11.76377,1622739665000
        |"1310",45.61284,11.76466,1622739675000
        |"1320",45.61369,11.76579,1622739685000
        |"1330",45.61398,11.76712,1622739695000
        |"1340",45.61416,11.76757,1622739705000
        |"1350",45.6151,11.76732,1622739715000
        |"1360",45.61603,11.76744,1622739725000
        |"1370",45.61645,11.76864,1622739735000
        |"1380",45.61652,11.7692,1622739745000
        |"1390",45.61704,11.76936,1622739755000
        |"1400",45.6185,11.76929,1622739765000
        |"1410",45.62024,11.76922,1622739775000
        |"1420",45.62204,11.76915,1622739785000
        |"1430",45.62315,11.76911,1622739795000
        |"1440",45.62459,11.76905,1622739805000
        |"1450",45.62632,11.76899,1622739815000
        |"1460",45.6278,11.76793,1622739825000
        |"1470",45.62862,11.76574,1622739835000
        |"1480",45.62915,11.76339,1622739845000
        |"1490",45.62947,11.76231,1622739855000
        |"1500",45.63066,11.76124,1622739865000
        |"1510",45.63205,11.76086,1622739875000
        |"1520",45.63238,11.76188,1622739885000
        |"1530",45.63258,11.76373,1622739895000
        |"1540",45.63261,11.76457,1622739905000
        |"1550",45.63292,11.76458,1622739915000
        |"1560",45.63424,11.76429,1622739925000
        |"1570",45.6357,11.76418,1622739935000
        |"1580",45.63689,11.76396,1622739945000
        |"1590",45.63747,11.76383,1622739955000
        |"1600",45.63767,11.76488,1622739965000
        |"1610",45.63785,11.76645,1622739975000
        |"1620",45.63785,11.76683,1622739985000
        |"1630",45.63781,11.76684,1622739995000
        |"1640",45.63754,11.76686,1622740005000
        |"1650",45.63753,11.76687,1622740015000
        |"1660",45.63754,11.76688,1622740025000
        |"1670",45.63753,11.76688,1622740035000
        |"1680",45.63753,11.76688,1622740045000
        |"1690",45.63753,11.76688,1622740055000
        |"1700",45.63753,11.76688,1622740065000
        |"1710",45.63753,11.76688,1622740075000
        |"1720",45.63755,11.76683,1622740085000
        |"1730",45.63758,11.76686,1622740095000
        |"1740",45.63759,11.76683,1622740105000
        |"1750",45.63757,11.76682,1622740115000
        |"1760",45.63759,11.76684,1622740125000
        |"1770",45.63757,11.76682,1622740135000
        |"1780",45.63756,11.76681,1622740145000
        |"1790",45.63756,11.76682,1622740155000
        |"1800",45.63755,11.7668,1622740165000
        |"1810",45.63754,11.76679,1622740175000
        |"1820",45.63755,11.76679,1622740185000
        |"1830",45.63756,11.76676,1622740195000
        |"1840",45.63756,11.76674,1622740205000
        |"1850",45.63757,11.76676,1622740215000
        |"1860",45.63756,11.76676,1622740225000
        |"1870",45.63757,11.76673,1622740235000
        |"""
    )

    val result = manager.matchingRoute(points).right.value

    result.distanceBetweenPoints.length should be (result.points.length - 1)

    distanceBetween(result, 9900d, 10100d)

    distanceBetween(result.distanceBetweenPoints.head, 70d, 80d)
    result.distanceBetweenPoints(1).distance shouldBe None
    distanceBetween(result.distanceBetweenPoints(2), 360d, 370d)
    distanceBetween(result.distanceBetweenPoints(3), 150d, 160d)
    result.distanceBetweenPoints(4).distance shouldBe None
    distanceBetween(result.distanceBetweenPoints(5), 260d, 270d)
    // ...
  }

  it should "perform distance between points accurately - nola" taggedAs Slow in {
    val points = csvToPoints(
      """
        |"0",40.87608,14.4976,1622737321000
        |"10",40.87521,14.49661,1622737332000
        |"20",40.87518,14.49676,1622737342000
        |"30",40.87517,14.49676,1622737352000
        |"40",40.87515,14.49679,1622737362000
        |"50",40.87514,14.49681,1622737372000
        |"60",40.87514,14.49681,1622737382000
        |"70",40.87514,14.49681,1622737392000
        |"80",40.87514,14.49681,1622737402000
        |"90",40.87514,14.49684,1622737412000
        |"100",40.87514,14.49684,1622737422000
        |"110",40.87525,14.49697,1622737432000
        |"120",40.87525,14.49697,1622737442000
        |"130",40.87535,14.49701,1622737452000
        |"140",40.87534,14.49701,1622737462000
        |"150",40.87534,14.49701,1622737472000
        |"160",40.87535,14.49701,1622737482000
        |"170",40.87535,14.49701,1622737492000
        |"180",40.87535,14.49701,1622737502000
        |"190",40.87535,14.49702,1622737512000
        |"200",40.87535,14.49702,1622737522000
        |"210",40.87535,14.49702,1622737532000
        |"220",40.87535,14.49702,1622737542000
        |"230",40.87532,14.49775,1622737552000
        |"240",40.87513,14.49906,1622737562000
        |"250",40.87478,14.50045,1622737572000
        |"260",40.87455,14.50176,1622737582000
        |"270",40.87439,14.50259,1622737592000
        |"280",40.87434,14.50278,1622737602000
        |"290",40.8742,14.50366,1622737612000
        |"300",40.87422,14.50464,1622737622000
        |"310",40.87445,14.50601,1622737632000
        |"320",40.87465,14.50743,1622737642000
        |"330",40.87465,14.50875,1622737652000
        |"340",40.87462,14.51003,1622737662000
        |"350",40.87465,14.51124,1622737672000
        |"360",40.87462,14.51174,1622737682000
        |"370",40.8748,14.51175,1622737692000
        |"380",40.87523,14.51175,1622737702000
        |"390",40.87568,14.51173,1622737712000
        |"400",40.87641,14.51176,1622737722000
        |"410",40.87705,14.51173,1622737732000
        |"420",40.87797,14.51173,1622737742000
        |"430",40.87893,14.51178,1622737752000
        |"440",40.87999,14.51173,1622737762000
        |"450",40.88113,14.51172,1622737772000
        |"460",40.88233,14.51172,1622737782000
        |"470",40.88346,14.51167,1622737792000
        |"480",40.88433,14.51162,1622737802000
        |"490",40.88506,14.51161,1622737812000
        |"500",40.88558,14.51218,1622737822000
        |"510",40.88633,14.51305,1622737832000
        |"520",40.88714,14.51394,1622737842000
        |"530",40.88773,14.51495,1622737852000
        |"540",40.88825,14.51623,1622737862000
        |"550",40.88903,14.51751,1622737872000
        |"560",40.88997,14.51889,1622737882000
        |"570",40.89096,14.52031,1622737892000
        |"580",40.892,14.52163,1622737902000
        |"590",40.89327,14.5225,1622737912000
        |"600",40.89446,14.52318,1622737922000
        |"610",40.89582,14.52377,1622737932000
        |"620",40.89728,14.52386,1622737942000
        |"630",40.89874,14.52445,1622737952000
        |"640",40.90023,14.52505,1622737962000
        |"650",40.90166,14.52562,1622737972000
        |"660",40.90275,14.52676,1622737982000
        |"670",40.90394,14.52776,1622737992000
        |"680",40.90492,14.52858,1622738002000
        |"690",40.90524,14.52888,1622738012000
        |"700",40.90612,14.5287,1622738022000
        |"710",40.90717,14.52826,1622738032000
        |"720",40.90846,14.52813,1622738042000
        |"730",40.90983,14.528,1622738052000
        |"740",40.91108,14.52763,1622738062000
        |"750",40.91241,14.52728,1622738072000
        |"760",40.91354,14.52694,1622738082000
        |"770",40.91365,14.52652,1622738092000
        |"780",40.91335,14.52665,1622738102000
        |"790",40.91335,14.52667,1622738112000
        |"800",40.91335,14.52667,1622738122000
        |"810",40.91335,14.52667,1622738132000
        |"820",40.91335,14.52667,1622738142000
        |"830",40.91335,14.52667,1622738152000
        |"840",40.91335,14.52667,1622738162000
        |"850",40.91335,14.52667,1622738172000
        |"860",40.91335,14.52667,1622738182000
        |"870",40.91335,14.52667,1622738192000
        |"880",40.91335,14.52667,1622738202000
        |"890",40.91335,14.52668,1622738212000
        |"900",40.91335,14.52668,1622738222000
        |"910",40.91335,14.52668,1622738232000
        |"920",40.91335,14.52668,1622738242000
        |"930",40.91335,14.52668,1622738252000
        |"940",40.91335,14.52668,1622738262000
        |"950",40.91335,14.52668,1622738272000
        |"960",40.91335,14.52668,1622738282000
        |"970",40.91335,14.52668,1622738292000
        |"980",40.91334,14.52671,1622738302000
        |"990",40.91337,14.52687,1622738312000
        |"1000",40.91337,14.52688,1622738322000
        |"1010",40.91337,14.52689,1622738332000
        |"1020",40.913,14.52704,1622738342000
        |"1030",40.91204,14.52736,1622738352000
        |"1040",40.91116,14.52764,1622738362000
        |"1050",40.90962,14.52797,1622738372000
        |"1060",40.90856,14.52813,1622738382000
        |"1070",40.90747,14.52823,1622738392000
        |"1080",40.90642,14.52846,1622738402000
        |"1090",40.90536,14.52871,1622738412000
        |"1100",40.90443,14.52811,1622738422000
        |"1110",40.90343,14.52731,1622738432000
        |"1120",40.90247,14.52641,1622738442000
        |"1130",40.90147,14.52542,1622738452000
        |"1140",40.90028,14.52497,1622738462000
        |"1150",40.89921,14.52454,1622738472000
        |"1160",40.89829,14.52414,1622738482000
        |"1170",40.89723,14.52373,1622738492000
        |"1180",40.89569,14.52365,1622738502000
        |"1190",40.89428,14.52296,1622738512000
        |"1200",40.89286,14.52213,1622738522000
        |"1210",40.89166,14.52115,1622738532000
        |"1220",40.89081,14.52,1622738542000
        |"1230",40.89007,14.51895,1622738552000
        |"1240",40.88928,14.5178,1622738562000
        |"1250",40.88843,14.51652,1622738572000
        |"1260",40.88778,14.51505,1622738582000
        |"1270",40.88734,14.51488,1622738592000
        |"1280",40.88651,14.51571,1622738602000
        |"1290",40.88568,14.51651,1622738612000
        |"1300",40.88507,14.51672,1622738622000
        |"1310",40.88461,14.51613,1622738632000
        |"1320",40.88425,14.51574,1622738642000
        |"1330",40.88362,14.51505,1622738652000
        |"1340",40.88297,14.51432,1622738662000
        |"1350",40.88233,14.51358,1622738672000
        |"1360",40.88168,14.51283,1622738682000
        |"1370",40.88113,14.51219,1622738692000
        |"1380",40.88081,14.51221,1622738702000
        |"1390",40.88024,14.51261,1622738712000
        |"1400",40.87981,14.5128,1622738722000
        |"1410",40.87925,14.5122,1622738732000
        |"1420",40.87884,14.5119,1622738742000
        |"1430",40.87885,14.51178,1622738752000
        |"1440",40.87848,14.5117,1622738762000
        |"1450",40.87747,14.51173,1622738772000
        |"1460",40.87697,14.51158,1622738782000
        |"1470",40.87734,14.51114,1622738792000
        |"1480",40.87688,14.51051,1622738802000
        |"1490",40.87613,14.50966,1622738812000
        |"1500",40.87535,14.50874,1622738822000
        |"1510",40.87477,14.50809,1622738832000
        |"1520",40.87465,14.50779,1622738842000
        |"1530",40.87454,14.5063,1622738852000
        |"1540",40.87423,14.5044,1622738862000
        |"1550",40.87425,14.50348,1622738872000
        |"1560",40.87436,14.50273,1622738882000
        |"1570",40.87445,14.50239,1622738892000
        |"1580",40.87451,14.50197,1622738902000
        |"1590",40.87454,14.50169,1622738912000
        |"1600",40.87457,14.50147,1622738922000
        |"1610",40.87476,14.50049,1622738932000
        |"1620",40.87514,14.49901,1622738942000
        |"1630",40.87537,14.4974,1622738952000
        |"1640",40.87512,14.49689,1622738962000
        |"1650",40.87512,14.49687,1622738972000
        |"1660",40.87512,14.49687,1622738982000
        |"1670",40.87524,14.49659,1622738992000
        |""".stripMargin
    )

    val result = manager.matchingRoute(points).right.value

    distanceBetween(result, 12400d, 12600d)

    result.distanceBetweenPoints.head.distance shouldBe None
    distanceBetween(result.distanceBetweenPoints(1), 310d, 330d)
    distanceBetween(result.distanceBetweenPoints(2), 120d, 130d)
    distanceBetween(result.distanceBetweenPoints(3), 110d, 120d)
    distanceBetween(result.distanceBetweenPoints(4), 160d, 170d)
    distanceBetween(result.distanceBetweenPoints(5), 195d, 205d)
    // ...
  }

  it should "perform distance between points accurately - palonetto" in {

    val points = csvToPoints(
      """
        |"0",40.82353,14.21931,1622737898000
        |"10",40.82357,14.21929,1622737909000
        |"20",40.82353,14.21927,1622737919000
        |"30",40.82351,14.21925,1622737929000
        |"40",40.82358,14.21934,1622737939000
        |"50",40.82378,14.21971,1622737949000
        |"60",40.82411,14.22023,1622737959000
        |"70",40.82454,14.22069,1622737969000
        |"80",40.82485,14.22104,1622737979000
        |"90",40.82536,14.22086,1622737989000
        |"100",40.82556,14.22066,1622737999000
        |"110",40.82558,14.22064,1622738009000
        |"120",40.82563,14.22061,1622738019000
        |"130",40.82563,14.22062,1622738029000
        |"140",40.82565,14.22062,1622738039000
        |"150",40.82567,14.2206,1622738049000
        |"160",40.82567,14.22059,1622738059000
        |"170",40.82568,14.22059,1622738069000
        |"180",40.82572,14.22055,1622738079000
        |"190",40.82579,14.22048,1622738089000
        |"200",40.82582,14.22047,1622738099000
        |"210",40.82586,14.22045,1622738109000
        |"220",40.82594,14.22043,1622738119000
        |"230",40.82602,14.22038,1622738129000
        |"240",40.82602,14.22036,1622738139000
        |"250",40.82603,14.22036,1622738149000
        |"260",40.82603,14.22035,1622738159000
        |"270",40.82624,14.2203,1622738169000
        |"280",40.82628,14.22029,1622738179000
        |"290",40.82635,14.22028,1622738189000
        |"300",40.82639,14.22028,1622738199000
        |"310",40.82657,14.22026,1622738209000
        |"320",40.82658,14.22026,1622738219000
        |"330",40.82666,14.22027,1622738229000
        |"340",40.82691,14.22028,1622738239000
        |"350",40.82702,14.22031,1622738249000
        |"360",40.82758,14.22059,1622738259000
        |"370",40.82793,14.22092,1622738269000
        |"380",40.82833,14.2215,1622738279000
        |"390",40.82869,14.22198,1622738289000
        |"400",40.829,14.22246,1622738299000
        |"410",40.82942,14.22313,1622738309000
        |"420",40.82983,14.22385,1622738319000
        |"430",40.83014,14.22449,1622738329000
        |"440",40.83037,14.22492,1622738339000
        |"450",40.83058,14.2254,1622738349000
        |"460",40.83069,14.22559,1622738359000
        |"470",40.83068,14.2256,1622738369000
        |"480",40.83077,14.22583,1622738379000
        |"490",40.83094,14.22615,1622738389000
        |"500",40.831,14.22633,1622738399000
        |"510",40.83107,14.2265,1622738409000
        |"520",40.83106,14.22652,1622738419000
        |"530",40.83125,14.2267,1622738429000
        |"540",40.83145,14.22662,1622738439000
        |"550",40.83174,14.2265,1622738449000
        |"560",40.83197,14.2262,1622738459000
        |"570",40.83159,14.22564,1622738469000
        |"580",40.83133,14.22497,1622738479000
        |"590",40.83117,14.22454,1622738489000
        |"600",40.83079,14.22385,1622738500000
        |"610",40.83048,14.22319,1622738510000
        |"620",40.83029,14.22271,1622738520000
        |"630",40.83009,14.22225,1622738530000
        |"640",40.83008,14.2222,1622738540000
        |"650",40.82999,14.22206,1622738550000
        |"660",40.82981,14.22177,1622738560000
        |"670",40.82962,14.22139,1622738570000
        |"680",40.82953,14.22126,1622738580000
        |"690",40.82942,14.22099,1622738590000
        |"700",40.82929,14.22066,1622738600000
        |"710",40.82912,14.22015,1622738610000
        |"720",40.82907,14.21987,1622738620000
        |"730",40.82903,14.2198,1622738630000
        |"740",40.82885,14.21977,1622738640000
        |"750",40.82856,14.22011,1622738650000
        |"760",40.82834,14.22055,1622738660000
        |"770",40.82828,14.22056,1622738670000
        |"780",40.82809,14.22072,1622738680000
        |"790",40.82797,14.22102,1622738690000
        |"800",40.8276,14.22065,1622738700000
        |"810",40.82738,14.22031,1622738710000
        |"820",40.82739,14.2203,1622738720000
        |"830",40.82739,14.2203,1622738730000
        |"840",40.82733,14.22024,1622738740000
        |"850",40.82696,14.21993,1622738750000
        |"860",40.82661,14.21979,1622738760000
        |"870",40.82608,14.21976,1622738770000
        |"880",40.82548,14.22009,1622738780000
        |"890",40.8249,14.22037,1622738790000
        |"900",40.82428,14.22015,1622738800000
        |"910",40.82368,14.21933,1622738810000
        |"920",40.8235,14.21851,1622738820000
        |"930",40.8236,14.21949,1622738830000
        |"940",40.82404,14.22022,1622738840000
        |"950",40.82433,14.22056,1622738850000
        |"960",40.82472,14.22101,1622738860000
        |"970",40.82493,14.22108,1622738870000
        |"980",40.82497,14.22107,1622738880000
        |"990",40.8251,14.22104,1622738890000
        |"1000",40.82524,14.22098,1622738900000
        |"1010",40.82538,14.22087,1622738910000
        |"1020",40.82539,14.22086,1622738920000
        |"1030",40.82539,14.22086,1622738930000
        |"1040",40.82541,14.22084,1622738940000
        |"1050",40.82551,14.22075,1622738950000
        |"1060",40.82564,14.22064,1622738960000
        |"1070",40.82565,14.22064,1622738970000
        |"1080",40.82586,14.22055,1622738980000
        |"1090",40.82591,14.22053,1622738990000
        |"1100",40.82591,14.22052,1622739000000
        |"1110",40.8259,14.22053,1622739010000
        |"1120",40.82591,14.22053,1622739020000
        |"1130",40.82611,14.22043,1622739030000
        |"1140",40.82614,14.22042,1622739040000
        |"1150",40.82624,14.22037,1622739050000
        |"1160",40.82626,14.22035,1622739060000
        |"1170",40.82651,14.22031,1622739070000
        |"1180",40.82659,14.22032,1622739080000
        |"1190",40.82667,14.22031,1622739090000
        |"1200",40.82689,14.22032,1622739100000
        |"1210",40.82733,14.22046,1622739110000
        |"1220",40.82785,14.22086,1622739120000
        |"1230",40.82785,14.22084,1622739130000
        |"1240",40.82785,14.22083,1622739140000
        |"1250",40.82808,14.22113,1622739150000
        |"1260",40.82854,14.22178,1622739160000
        |"1270",40.82874,14.22207,1622739170000
        |"1280",40.82886,14.22224,1622739180000
        |"1290",40.8291,14.22262,1622739190000
        |"1300",40.82946,14.22321,1622739200000
        |"1310",40.82984,14.22385,1622739210000
        |"1320",40.83,14.22407,1622739220000
        |"1330",40.83001,14.22408,1622739230000
        |"1340",40.83025,14.22451,1622739240000
        |"1350",40.83064,14.22531,1622739250000
        |"1360",40.83091,14.22601,1622739260000
        |"1370",40.83098,14.22619,1622739270000
        |"1380",40.83107,14.22644,1622739280000
        |"1390",40.83111,14.22666,1622739290000
        |"1400",40.83113,14.22668,1622739300000
        |"1410",40.83134,14.22669,1622739310000
        |"1420",40.83153,14.22661,1622739320000
        |"1430",40.83182,14.22658,1622739330000
        |"1440",40.83187,14.22667,1622739340000
        |"1450",40.83197,14.22697,1622739350000
        |"1460",40.83205,14.22712,1622739360000
        |"1470",40.83206,14.22721,1622739370000
        |"1480",40.83207,14.22722,1622739380000
        |"1490",40.83207,14.22723,1622739390000
        |"1500",40.83214,14.2274,1622739400000
        |"1510",40.83214,14.22745,1622739410000
        |"1520",40.83214,14.22745,1622739420000
        |"1530",40.83213,14.22751,1622739430000
        |"1540",40.83213,14.2275,1622739440000
        |"1550",40.83219,14.22771,1622739450000
        |"1560",40.83219,14.22779,1622739460000
        |"1570",40.8322,14.2278,1622739470000
        |"1580",40.8322,14.22781,1622739480000
        |"1590",40.83222,14.22783,1622739490000
        |"1600",40.83227,14.22801,1622739500000
        |"1610",40.83233,14.22834,1622739510000
        |"1620",40.83235,14.22847,1622739520000
        |"1630",40.83232,14.2285,1622739530000
        |"1640",40.8323,14.2285,1622739540000
        |"1650",40.83231,14.22851,1622739550000
        |"1660",40.8323,14.22851,1622739560000
        |"1670",40.83231,14.22864,1622739570000
        |"1680",40.83243,14.22892,1622739580000
        |"1690",40.83245,14.22914,1622739590000
        |"1700",40.83246,14.22912,1622739600000
        |"1710",40.83248,14.22925,1622739610000
        |"1720",40.83247,14.22924,1622739620000
        |"1730",40.83254,14.22934,1622739630000
        |"1740",40.83252,14.22963,1622739640000
        |"1750",40.83251,14.22972,1622739650000
        |"1760",40.83248,14.22996,1622739660000
        |"1770",40.83258,14.23025,1622739670000
        |"1780",40.83264,14.23044,1622739680000
        |"1790",40.83258,14.23055,1622739690000
        |"1800",40.83258,14.23059,1622739700000
        |"1810",40.83259,14.23061,1622739710000
        |"1820",40.83259,14.23081,1622739720000
        |"1830",40.83259,14.23109,1622739730000
        |"1840",40.83258,14.23107,1622739740000
        |"1850",40.83257,14.23125,1622739750000
        |"1860",40.83258,14.23152,1622739760000
        |"1870",40.83256,14.23154,1622739770000
        |"1880",40.83257,14.23153,1622739780000
        |"1890",40.83257,14.23154,1622739790000
        |"1900",40.83257,14.23153,1622739800000
        |"1910",40.83257,14.23157,1622739810000
        |"1920",40.83259,14.23192,1622739820000
        |"1930",40.83258,14.23205,1622739830000
        |"1940",40.83257,14.23206,1622739840000
        |"1950",40.83257,14.23223,1622739850000
        |"1960",40.83257,14.23233,1622739860000
        |"1970",40.83258,14.23234,1622739870000
        |"1980",40.83256,14.23248,1622739880000
        |"1990",40.83247,14.2328,1622739890000
        |"2000",40.83244,14.23291,1622739900000
        |"2010",40.83243,14.2329,1622739910000
        |"2020",40.83243,14.23289,1622739920000
        |"2030",40.83242,14.23289,1622739930000
        |"2040",40.8324,14.23301,1622739940000
        |"2050",40.8324,14.23301,1622739950000
        |"2060",40.8324,14.233,1622739960000
        |"2070",40.83235,14.23326,1622739970000
        |"2080",40.83231,14.23338,1622739980000
        |"2090",40.83231,14.23338,1622739990000
        |"2100",40.83228,14.23354,1622740000000
        |"2110",40.83227,14.23369,1622740010000
        |"2120",40.83222,14.23384,1622740020000
        |"2130",40.83222,14.23384,1622740030000
        |"2140",40.83219,14.23404,1622740040000
        |"2150",40.83218,14.23421,1622740050000
        |"2160",40.83218,14.23422,1622740060000
        |"2170",40.83219,14.23439,1622740070000
        |"2180",40.83218,14.2344,1622740080000
        |"2190",40.83218,14.23442,1622740090000
        |"2200",40.83214,14.23486,1622740100000
        |"2210",40.83213,14.23503,1622740110000
        |"2220",40.8321,14.23528,1622740120000
        |"2230",40.83209,14.23541,1622740130000
        |"2240",40.83207,14.23543,1622740140000
        |"2250",40.83207,14.23546,1622740150000
        |"2260",40.83207,14.23547,1622740160000
        |"2270",40.83206,14.23547,1622740170000
        |"2280",40.83208,14.23573,1622740180000
        |"2290",40.83207,14.23573,1622740190000
        |"2300",40.83205,14.23585,1622740200000
        |"2310",40.83205,14.23587,1622740210000
        |"2320",40.83203,14.23612,1622740220000
        |"2330",40.83201,14.23625,1622740230000
        |"2340",40.83202,14.23626,1622740240000
        |"2350",40.83201,14.23647,1622740250000
        |"2360",40.83197,14.23682,1622740260000
        |"2370",40.83196,14.23696,1622740270000
        |"2380",40.83195,14.23701,1622740280000
        |"2390",40.83195,14.23701,1622740290000
        |"2400",40.83193,14.23728,1622740300000
        |"2410",40.83191,14.23745,1622740310000
        |"2420",40.83191,14.23749,1622740320000
        |"2430",40.83191,14.23749,1622740330000
        |"2440",40.83191,14.23749,1622740340000
        |"2450",40.83189,14.23766,1622740350000
        |"2460",40.83189,14.2377,1622740360000
        |"2470",40.83189,14.23772,1622740370000
        |"2480",40.83189,14.23772,1622740380000
        |"2490",40.83189,14.23772,1622740390000
        |"2500",40.83188,14.23785,1622740400000
        |"2510",40.83187,14.23785,1622740410000
        |"2520",40.83188,14.23785,1622740420000
        |"2530",40.83184,14.23819,1622740430000
        |"2540",40.83184,14.2382,1622740440000
        |"2550",40.83183,14.23822,1622740450000
        |"2560",40.83183,14.23827,1622740460000
        |"2570",40.83183,14.23834,1622740470000
        |"2580",40.83183,14.23843,1622740480000
        |"2590",40.83182,14.23849,1622740490000
        |"2600",40.8318,14.2386,1622740500000
        |"2610",40.83179,14.23883,1622740510000
        |"2620",40.83177,14.2389,1622740520000
        |"2630",40.83177,14.2389,1622740530000
        |"2640",40.83178,14.23916,1622740540000
        |"2650",40.83175,14.23924,1622740550000
        |"2660",40.83174,14.23927,1622740560000
        |"2670",40.83175,14.23942,1622740570000
        |"2680",40.83172,14.23957,1622740580000
        |"2690",40.83171,14.23955,1622740590000
        |"2700",40.8317,14.23978,1622740600000
        |"2710",40.83169,14.23993,1622740610000
        |"2720",40.83168,14.23992,1622740620000
        |"2730",40.83168,14.24017,1622740630000
        |"2740",40.83167,14.24025,1622740640000
        |"2750",40.83162,14.24035,1622740650000
        |"2760",40.83163,14.2404,1622740660000
        |"2770",40.83162,14.24048,1622740670000
        |"2780",40.83161,14.24068,1622740680000
        |"2790",40.83163,14.24073,1622740690000
        |"2800",40.83163,14.24082,1622740700000
        |"2810",40.83158,14.24091,1622740710000
        |"2820",40.83155,14.24099,1622740720000
        |"2830",40.83155,14.2411,1622740730000
        |"2840",40.83154,14.24112,1622740740000
        |"2850",40.83154,14.24122,1622740750000
        |"2860",40.8315,14.24132,1622740760000
        |"2870",40.83147,14.2414,1622740770000
        |"2880",40.83149,14.24148,1622740780000
        |"2890",40.83148,14.24166,1622740790000
        |"2900",40.83144,14.24199,1622740800000
        |"2910",40.83138,14.24265,1622740810000
        |"2920",40.83128,14.24323,1622740820000
        |"2930",40.83122,14.24348,1622740830000
        |"2940",40.83102,14.24402,1622740840000
        |"2950",40.83057,14.24443,1622740850000
        |"2960",40.83007,14.24487,1622740860000
        |"2970",40.82998,14.24507,1622740870000
        |"2980",40.82996,14.24564,1622740880000
        |"2990",40.82994,14.24616,1622740890000
        |"3000",40.82989,14.24675,1622740900000
        |"3010",40.82996,14.24704,1622740910000
        |"3020",40.82994,14.24704,1622740920000
        |"3030",40.82994,14.24721,1622740930000
        |"3040",40.82998,14.24732,1622740940000
        |"3050",40.82994,14.2476,1622740950000
        |"3060",40.82992,14.24779,1622740960000
        |"3070",40.82994,14.24804,1622740970000
        |"3080",40.82991,14.2482,1622740980000
        |"3090",40.82994,14.24841,1622740990000
        |"3100",40.82994,14.24855,1622741000000
        |"3110",40.82989,14.24861,1622741010000
        |"3120",40.82987,14.2489,1622741020000
        |"3130",40.82984,14.24892,1622741030000
        |"3140",40.82981,14.24911,1622741040000
        |"3150",40.82975,14.24915,1622741050000
        |"3160",40.82975,14.24918,1622741060000
        |"3170",40.82973,14.24918,1622741070000
        |"3180",40.82973,14.24925,1622741080000
        |"3190",40.82985,14.24964,1622741090000
        |"3200",40.8299,14.24975,1622741100000
        |"3210",40.8299,14.24981,1622741110000
        |"3220",40.82991,14.24983,1622741120000
        |"3230",40.82995,14.24994,1622741130000
        |"3240",40.82998,14.24997,1622741140000
        |"3250",40.83006,14.25014,1622741150000
        |"3260",40.83009,14.25012,1622741160000
        |"3270",40.83026,14.25011,1622741170000
        |"3280",40.83024,14.25015,1622741180000
        |"3290",40.83023,14.25013,1622741190000
        |"3300",40.83035,14.25017,1622741200000
        |"3310",40.83046,14.25012,1622741210000
        |"3320",40.83049,14.25009,1622741220000
        |"3330",40.83054,14.25024,1622741230000
        |"3340",40.83065,14.25029,1622741240000
        |"3350",40.8307,14.2503,1622741250000
        |"3360",40.83072,14.25036,1622741260000
        |"3370",40.83074,14.2504,1622741270000
        |"3380",40.83084,14.25054,1622741280000
        |"3390",40.83096,14.25061,1622741290000
        |"3400",40.83119,14.25052,1622741300000
        |"3410",40.83133,14.25071,1622741310000
        |"3420",40.83146,14.25073,1622741320000
        |"3430",40.83158,14.25085,1622741330000
        |"3440",40.83163,14.25085,1622741340000
        |"3450",40.83168,14.25092,1622741350000
        |"3460",40.83178,14.25108,1622741360000
        |"3470",40.83193,14.25117,1622741370000
        |"3480",40.83205,14.25128,1622741380000
        |"3490",40.83215,14.25124,1622741390000
        |"3500",40.83234,14.25126,1622741400000
        |"3510",40.83247,14.25125,1622741410000
        |"3520",40.83248,14.25141,1622741420000
        |"3530",40.83262,14.25152,1622741430000
        |"3540",40.83286,14.25172,1622741440000
        |"3550",40.83295,14.25171,1622741450000
        |"3560",40.83315,14.25172,1622741460000
        |"3570",40.83349,14.25208,1622741470000
        |"3580",40.83392,14.2517,1622741480000
        |"3590",40.83429,14.25129,1622741490000
        |"3600",40.83465,14.25094,1622741500000
        |"3610",40.83492,14.25069,1622741510000
        |"3620",40.8352,14.2504,1622741520000
        |"3630",40.83546,14.25049,1622741530000
        |"3640",40.83588,14.25104,1622741540000
        |"3650",40.83638,14.25185,1622741550000
        |"3660",40.83665,14.25268,1622741560000
        |"3670",40.83679,14.25294,1622741570000
        |"3680",40.83685,14.25312,1622741580000
        |"3690",40.83685,14.25312,1622741590000
        |"3700",40.83685,14.25311,1622741600000
        |"3710",40.83686,14.25314,1622741610000
        |"3720",40.83695,14.25343,1622741620000
        |"3730",40.83711,14.25362,1622741630000
        |"3740",40.8373,14.25371,1622741640000
        |"3750",40.83753,14.25396,1622741650000
        |"3760",40.83772,14.25414,1622741660000
        |"3770",40.83777,14.25416,1622741670000
        |"3780",40.83778,14.25417,1622741680000
        |"3790",40.83779,14.25418,1622741690000
        |"3800",40.83808,14.25433,1622741700000
        |"3810",40.83856,14.25471,1622741710000
        |"3820",40.83919,14.25516,1622741720000
        |"3830",40.83944,14.25523,1622741730000
        |"3840",40.83945,14.25517,1622741740000
        |"3850",40.83948,14.25519,1622741750000
        |"3860",40.83957,14.2553,1622741760000
        |"3870",40.83987,14.25552,1622741770000
        |"3880",40.84012,14.25571,1622741780000
        |"3890",40.84068,14.25607,1622741790000
        |"3900",40.84126,14.25657,1622741800000
        |"3910",40.84173,14.25693,1622741810000
        |"3920",40.84174,14.25693,1622741820000
        |"3930",40.84174,14.25693,1622741830000
        |"3940",40.84176,14.25696,1622741840000
        |""".stripMargin
    )

    manager.matchingRoute(points).left.value
  }

  it should "perform distance between points accurately - manerbio" taggedAs Slow in {
    val points = csvToPoints(
      """|
         |"0",45.3721,10.0186,1624197792000
         |"10",45.37217,10.01882,1624197803000
         |"20",45.37245,10.01911,1624197813000
         |"30",45.37239,10.01969,1624197823000
         |"40",45.37173,10.0207,1624197833000
         |"50",45.37122,10.02202,1624197843000
         |"60",45.37078,10.02334,1624197853000
         |"70",45.37049,10.0244,1624197863000
         |"80",45.37033,10.02504,1624197873000
         |"90",45.37004,10.02625,1624197883000
         |"100",45.36993,10.02788,1624197893000
         |"110",45.36951,10.02957,1624197903000
         |"120",45.36904,10.03123,1624197913000
         |"130",45.36856,10.03282,1624197923000
         |"140",45.36818,10.0341,1624197933000
         |"150",45.36778,10.03542,1624197943000
         |"160",45.36702,10.03685,1624197953000
         |"170",45.36608,10.03817,1624197963000
         |"180",45.36531,10.03969,1624197973000
         |"190",45.3646,10.04126,1624197983000
         |"200",45.36394,10.04273,1624197993000
         |"210",45.36333,10.04401,1624198003000
         |"220",45.36289,10.04482,1624198013000
         |"230",45.3627,10.04544,1624198023000
         |"240",45.36232,10.04684,1624198033000
         |"250",45.36181,10.04855,1624198043000
         |"260",45.36134,10.04954,1624198053000
         |"270",45.36046,10.05109,1624198063000
         |"280",45.35975,10.05308,1624198073000
         |"290",45.35938,10.05523,1624198083000
         |"300",45.35899,10.05684,1624198093000
         |"310",45.35886,10.05772,1624198103000
         |"320",45.35879,10.05938,1624198113000
         |"330",45.3587,10.061,1624198123000
         |"340",45.35874,10.06209,1624198133000
         |"350",45.35886,10.0639,1624198143000
         |"360",45.35864,10.06617,1624198153000
         |"370",45.3579,10.06853,1624198163000
         |"380",45.35715,10.07102,1624198173000
         |"390",45.3564,10.0736,1624198183000
         |"400",45.35607,10.07636,1624198193000
         |"410",45.35574,10.07919,1624198203000
         |"420",45.35541,10.08208,1624198213000
         |"430",45.35523,10.08474,1624198223000
         |"440",45.35502,10.08739,1624198233000
         |"450",45.35448,10.09002,1624198243000
         |"460",45.35405,10.09191,1624198253000
         |"470",45.35389,10.09221,1624198263000
         |"480",45.35373,10.09344,1624198273000
         |"490",45.35329,10.09546,1624198283000
         |"500",45.35277,10.09787,1624198293000
         |"510",45.35249,10.10056,1624198303000
         |"520",45.35272,10.10346,1624198313000
         |"530",45.35327,10.10609,1624198323000
         |"540",45.35378,10.1085,1624198333000
         |"550",45.35429,10.111,1624198343000
         |"560",45.35428,10.11362,1624198353000
         |"570",45.35402,10.11634,1624198363000
         |"580",45.35386,10.11896,1624198373000
         |"590",45.35391,10.12085,1624198383000
         |""".stripMargin
    )

    val result = manager.matchingRoute(points).right.value

    distanceBetween(result, 8600d, 8620d)
  }

  it should "match route" in {
    val points = csvToPoints(
      """
        |43.99579,12.53889,1549815047000000
        | 43.9978,12.54097,1549815129000000
        |44.01095,12.54593,1549815208000000
        |44.01302,12.54663,1549815300000000
        |44.02456,12.54961,1549815359000000
        |44.02975,12.55466,1549815424000000
        |44.03023,12.55512,1549815427000000
        | 44.0305,12.55539,1549815430000000
        |44.03068,12.55558,1549815472000000
        |44.03425,12.56014,1549815536000000
        | 44.0347,12.56078,1549815541000000
        |44.03948,12.56769,1549815618000000
        |44.04122,12.57017,1549815633000000
        |44.04137,12.57038,1549815704000000
        |44.04402,12.56026,1549815778000000
        |44.04631, 12.5531,1549815836000000
        |44.05471,12.54391,1549815898000000
        |44.06429,12.54142,1549815960000000
        |44.06528, 12.5321,1549816030000000
        |44.06983,12.50352,1549816130000000
        |44.06868,12.48335,1549816230000000
        |44.06753,12.47055,1549816309000000
        |44.06527,12.46188,1549816369000000
        |44.06466,12.45079,1549816447000000
        | 44.0676, 12.4393,1549816537000000
        |44.07291,12.42985,1549816614000000
        |44.08577,12.41739,1549816714000000
        |44.09295,12.40582,1549816810000000
        |44.09371,12.39774,1549816901000000
        |44.09208,12.38663,1549817001000000
        |44.09192,12.36915,1549817101000000
        |44.09531,12.36177,1549817145000000
        |44.09936,12.34788,1549817219000000
        |44.10352,12.33736,1549817294000000
        | 44.1051, 12.3338,1549817394000000
        |44.10954,12.31908,1549817494000000
        |44.11542,12.30485,1549817579000000
        |44.13438,12.28689,1549817679000000
        |44.14362,12.25685,1549817779000000
        |44.15903, 12.2172,1549817926000000
        |44.16772,12.20587,1549818002000000
        |44.17021,12.20502,1549818024000000
        |44.16912,12.20355,1549818038000000
        |44.15904,12.19201,1549818123000000
        |44.16286,12.18736,1549818161000000
        |44.16611,12.17579,1549818234000000
        |44.17267,12.15994,1549818334000000
        | 44.1792,12.14437,1549818434000000
        |44.18331,12.13794,1549818489000000
        |44.19635,12.13023,1549818589000000
        |44.19356,12.11341,1549818689000000
        |44.19306,12.11191,1549818711000000
        |44.18641,12.11254,1549818763000000
        |44.17864,12.10683,1549818823000000
        |44.16688,12.10041,1549818909000000
        |44.15189,12.08648,1549819000000000
        | 44.1469,12.07305,1549819071000000
        |44.14698,12.07156,1549819084000000
        |44.14658,12.07143,1549819089000000
        |44.13801,12.06628,1549819155000000
        |44.13407, 12.0612,1549819201000000
        | 44.1213,12.05941,1549819292000000
        |44.11185,12.05618,1549819356000000
        |44.09822,12.04977,1549819453000000
        |44.09822,12.04976,1549819553000000
        |""".stripMargin
    )

    manager.matchingRoute(points).left.value
  }

  it should "match route (2)" in {
    val points = csvToPoints(
      """
        |40.90719,14.74997,1550840076000000
        |40.90953, 14.7424,1550840222000000
        |40.90822,14.69061,1550840484000000
        |40.93442,14.64013,1550840784000000
        | 40.9395,14.55014,1550841084000000
        | 40.9167,14.47193,1550841335000000
        |40.91471,14.46731,1550841555000000
        |40.90448,14.35791,1550841850000000
        | 40.9397,14.32565,1550842102000000
        |40.93479,14.30422,1550842222000000
        |40.93708,14.27997,1550842396000000
        |40.94619,14.27281,1550842572000000
        |""".stripMargin
    )

    manager.matchingRoute(points).left.value
  }

  it should "match route (3)" in {
    val points =
      csvToPoints(new String(Files.readAllBytes(Paths.get("src/test/resources/test-cases/match-route-3.csv"))))

    manager.matchingRoute(points).left.value
  }

  it should "match route (4)" in {
    val points =
      csvToPoints(new String(Files.readAllBytes(Paths.get("src/test/resources/test-cases/match-route-4.csv"))))

    manager.matchingRoute(points).left.value
  }

  it should "match route (5)" taggedAs Slow in {
    val points =
      csvToPoints(new String(Files.readAllBytes(Paths.get("src/test/resources/test-cases/match-route-5.csv"))))

    val res = manager.matchingRoute(points).right.value

    distanceBetween(res, 30000, 31000)
  }

  private def csvToPoints(points: String): Seq[GPSPoint] =
    points.stripMargin.lines
      .map(_.trim)
      .filter(_.nonEmpty)
      .filterNot(_.startsWith("#")) // Commenting system for coordinates
      .map(s => s.split(",").filter(_.nonEmpty))
      .zipWithIndex
      .map { case (p, idx) => new GPSPoint(p(1).toDouble, p(2).toDouble, None, idx.toLong) }
      .toList

  private def distanceBetween(r: MatchedRoute, min: Double, max: Double): Assertion = {
    r.distanceBetweenPoints.length should be (r.points.size - 1)

    r.length.value should be <= max
    r.length.value should be >= min

    val distancesSum = r.distanceBetweenPoints.map(_.distance.getOrElse(0d)).sum

    distancesSum should be <= max
    distancesSum should be >= min

    val times = r.points.zip(r.points.tail).map { case (p1, p2) => p2.time - p1.time }
    val diffTime = r.distanceBetweenPoints.map(_.diffTime)

    diffTime shouldBe times
  }

  private def distanceBetween(r: DistancePoint, min: Double, max: Double): Assertion = {
    r.distance.get should be <= max
    r.distance.get should be >= min

    r.diffTime shouldBe Math.abs(r.node1.time - r.node2.time)
  }

}
