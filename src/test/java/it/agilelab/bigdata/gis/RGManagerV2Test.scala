package it.agilelab.bigdata.gis

import it.agilelab.bigdata.gis.loader.ReverseGeocodingManager
import it.agilelab.bigdata.gis.models.Address
import org.scalatest.FunSuite

class RGManagerV2Test extends FunSuite{

  val path2 = "/home/stefano/IdeaProjects/Agile.GIS.Server/data"
  ReverseGeocodingManager.init(path2)

  test("RGManager second version (loading boundaries)"){

    val answer = ReverseGeocodingManager.reverseGeocode(45.995920, 13.304883)

    println(answer.city)
    println(answer.street)

    assert(!answer.city.isEmpty && !answer.street.isEmpty)

  }

  test("General report validation"){

    val points = Seq((40.913693, 8.704525),
      (45.995920, 13.304883),
      (45.637362, 11.788101),
      (37.985901, 12.974634),
      (43.859778, 11.054971),
      (40.356107, 18.175934),
      (45.075297, 7.643453),
      (41.480186, 13.810318),
      (45.676958, 9.410124),
      (45.482349, 8.948337),
      (40.798701, 14.377684),
      (45.117133, 7.652195),
      (45.442548, 9.085188),
      (45.614007, 9.219835),
      (43.943741, 10.915345),
      (45.605247, 8.710839),
      (38.138100, 13.350173),
      (46.485170, 10.310192),
      (45.048958, 11.785429))

    val RGAddresses: Seq[Address] = points.map(x => ReverseGeocodingManager.reverseGeocode(x._1, x._2, true))

    val realAddresses = Seq(Address(Some("Via Colle di Frigiano"), Some("Castelsardo"), Some("Sassari"), Some("Sardegna"), Some("Italia")),
      Address(Some("Via Roma"), Some("Pavia di Udine"), Some("Udine"), Some("Friuli Venezia Giulia"), Some("Italia")),
      Address(Some("Via Muri Bianchi"), Some("Cittadella"), Some("Padova"), Some("Veneto"), Some("Italia") ),
      Address(Some("Via Porta Palermo"), Some("Alcamo"), Some("Trapani"), Some("Sicilia"), Some("Italia") ),
      Address(Some("Via 16 Aprile"), Some("Prato"), Some("Prato"), Some("Toscana"), Some("Italia") ),
      Address(Some("Via Giuseppe Garibaldi"), Some("Lecce"), Some("Lecce"), Some("Puglia"), Some("Italia") ),
      Address(Some("Piazza Rivoli"), Some("Torino"), Some("Torino"), Some("Piemonte"), Some("Italia") ),
      Address(Some("Via Casilina"), Some("Cassino"), Some("Frosinone"), Some("Lazio"), Some("Italia") ),
      Address(Some("Via 4 novembre"), Some("Merate"), Some("Lecco"), Some("Lombardia"), Some("Italia") ),
      Address(Some("Via Milano"), Some("Vittuone"), Some("Milano"), Some("Lombardia"), Some("Italia") ),
      Address(Some("Via Enrico de Nicola"), Some("Torre del Greco"), Some("Napoli"), Some("Campania"), Some("Italia") ),
      Address(Some("Via Gaetano Amati"), Some("Venaria Reale"), Some("Torino"), Some("Piemonte"), Some("Italia") ),
      Address(Some("Via Amerigo Vespucci"), Some("Cesano Boscone"), Some("Milano"), Some("Lombardia"), Some("Italia") ),
      Address(Some("Via Carlo Forlanini"), Some("Desio"), Some("Monza e della Brianza"), Some("Lombardia"), Some("Italia") ),
      Address(Some("Via Marino Marini"), Some("Pistoia"), Some("Pistoia"), Some("Toscana"), Some("Italia") ),
      Address(Some("Via per Tornavento"), Some("Lonate Pozzolo"), Some("Varese"), Some("Lombardia"), Some("Italia") ),
      Address(Some("Via Marchese di Villabianca"), Some("Palermo"), Some("Palermo"), Some("Sicilia"), Some("Italia") ),
      Address(Some("Via Nazionale"), Some("Valdidentro"), Some("Sondrio"), Some("Lombardia"), Some("Italia") ),
      Address(Some("Viale Porta Po"), Some("Rovigo"), Some("Rovigo"), Some("Veneto"), Some("Italia") )
    )

    assert(RGAddresses.map(_.city) == realAddresses.map(_.city))
    assert(RGAddresses.map(_.county) == realAddresses.map(_.county))
    assert(RGAddresses.map(_.region) == realAddresses.map(_.region))
    assert(RGAddresses.map(_.country) == realAddresses.map(_.country))

    assert(RGAddresses.map(_.street) == realAddresses.map(_.street))

  }


}
