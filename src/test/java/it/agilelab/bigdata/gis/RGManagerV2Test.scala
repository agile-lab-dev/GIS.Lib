package it.agilelab.bigdata.gis

import it.agilelab.bigdata.gis.domain.loader.ReverseGeocodingManager
import it.agilelab.bigdata.gis.domain.models.Address
import org.scalatest.FunSuite

class RGManagerV2Test extends FunSuite{

  val path2 = "src/test/resources/"
  ReverseGeocodingManager.init(path2)

  test("RGManager second version (loading boundaries)"){

    val answer = ReverseGeocodingManager.reverseGeocode(45.995920, 13.304883)

    println(answer.city)
    println(answer.street)

    assert(!answer.city.isEmpty && !answer.street.isEmpty)

  }

  test("General report validation"){

    val points = Seq(
      (37.496343, 15.067200),
      (38.128873, 13.359253),
      (37.310851, 13.583665),
      (38.064311, 15.621957),
      (40.351657, 18.175206),
      (40.666426, 16.605022)
    )

    val result: Seq[Address] = points.map(x => ReverseGeocodingManager.reverseGeocode(x._1, x._2, true))


    result.foreach(x => println(x))

    val realAddresses = Seq(
      Address(Some("Via Colle di Frigiano"), Some("Castelsardo"), Some("Sassari"), Some("Sardegna"), Some("Italia")),
      Address(Some("Via Roma"), Some("Pavia di Udine"), Some("Udine"), Some("Friuli Venezia Giulia"), Some("Italia")),
      Address(Some("Via Muri Bianchi"), Some("Cittadella"), Some("Padova"), Some("Veneto"), Some("Italia") ),
      Address(Some("Via Porta Palermo"), Some("Alcamo"), Some("Trapani"), Some("Sicilia"), Some("Italia") ),
      Address(Some("Via 16 Aprile"), Some("Prato"), Some("Prato"), Some("Toscana"), Some("Italia") )
    )


//    val realAddresses = Seq(
//      Address(Some("Via Colle di Frigiano"), Some("Castelsardo"), Some("Sassari"), Some("Sardegna"), Some("Italia")),
//      Address(Some("Via Roma"), Some("Pavia di Udine"), Some("Udine"), Some("Friuli Venezia Giulia"), Some("Italia")),
//      Address(Some("Via Muri Bianchi"), Some("Cittadella"), Some("Padova"), Some("Veneto"), Some("Italia") ),
//      Address(Some("Via Porta Palermo"), Some("Alcamo"), Some("Trapani"), Some("Sicilia"), Some("Italia") ),
//      Address(Some("Via 16 Aprile"), Some("Prato"), Some("Prato"), Some("Toscana"), Some("Italia") ),
//      Address(Some("Via Giuseppe Garibaldi"), Some("Lecce"), Some("Lecce"), Some("Puglia"), Some("Italia") ),
//      Address(Some("Piazza Rivoli"), Some("Torino"), Some("Torino"), Some("Piemonte"), Some("Italia") ),
//      Address(Some("Via Casilina"), Some("Cassino"), Some("Frosinone"), Some("Lazio"), Some("Italia") ),
//      Address(Some("Via 4 novembre"), Some("Merate"), Some("Lecco"), Some("Lombardia"), Some("Italia") ),
//      Address(Some("Via Milano"), Some("Vittuone"), Some("Milano"), Some("Lombardia"), Some("Italia") ),
//      Address(Some("Via Enrico de Nicola"), Some("Torre del Greco"), Some("Napoli"), Some("Campania"), Some("Italia") ),
//      Address(Some("Via Gaetano Amati"), Some("Venaria Reale"), Some("Torino"), Some("Piemonte"), Some("Italia") ),
//      Address(Some("Via Amerigo Vespucci"), Some("Cesano Boscone"), Some("Milano"), Some("Lombardia"), Some("Italia") ),
//      Address(Some("Via Carlo Forlanini"), Some("Desio"), Some("Monza e della Brianza"), Some("Lombardia"), Some("Italia") ),
//      Address(Some("Via Marino Marini"), Some("Pistoia"), Some("Pistoia"), Some("Toscana"), Some("Italia") ),
//      Address(Some("Via per Tornavento"), Some("Lonate Pozzolo"), Some("Varese"), Some("Lombardia"), Some("Italia") ),
//      Address(Some("Via Marchese di Villabianca"), Some("Palermo"), Some("Palermo"), Some("Sicilia"), Some("Italia") ),
//      Address(Some("Via Nazionale"), Some("Valdidentro"), Some("Sondrio"), Some("Lombardia"), Some("Italia") ),
//      Address(Some("Viale Porta Po"), Some("Rovigo"), Some("Rovigo"), Some("Veneto"), Some("Italia") )
//    )
//
//    assert(RGAddresses.map(_.city) == realAddresses.map(_.city))
//    assert(RGAddresses.map(_.county) == realAddresses.map(_.county))
//    assert(RGAddresses.map(_.region) == realAddresses.map(_.region))
//    assert(RGAddresses.map(_.country) == realAddresses.map(_.country))
//
//    assert(RGAddresses.map(_.street) == realAddresses.map(_.street))

  }


}
