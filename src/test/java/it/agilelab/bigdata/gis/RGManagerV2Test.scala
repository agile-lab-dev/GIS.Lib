package it.agilelab.bigdata.gis

import it.agilelab.bigdata.gis.loader.ReverseGeocodingManager
import it.agilelab.bigdata.gis.models.PartialAddress
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

    val RGAddresses: Seq[PartialAddress] = points.map(x => ReverseGeocodingManager.reverseGeocode(x._1, x._2))

    val realAddresses = Seq(PartialAddress("Via Colle di Frigiano", "Castelsardo", "Sassari", "Sardegna", "Italia"),
      PartialAddress("Via Roma","Pavia di Udine", "Udine", "Friuli Venezia Giulia", "Italia"),
      PartialAddress("Via Muri Bianchi", "Cittadella", "Padova", "Veneto", "Italia"),
      PartialAddress("Via Porta Palermo", "Alcamo", "Trapani", "Sicilia", "Italia"),
      PartialAddress("Via 16 Aprile", "Prato", "Prato", "Toscana", "Italia"),
      PartialAddress("Via Giuseppe Garibaldi", "Lecce", "Lecce", "Puglia", "Italia"),
      PartialAddress("Piazza Rivoli", "Torino", "Torino", "Piemonte", "Italia"),
      PartialAddress("Via Casilina", "Cassino", "Frosinone", "Lazio", "Italia"),
      PartialAddress("Via 4 novembre", "Merate", "Lecco", "Lombardia", "Italia"),
      PartialAddress("Rotonda Pilota Lorenzo Bandini", "Vittuone", "Milano", "Lombardia", "Italia"),
      PartialAddress("Via Enrico de Nicola", "Torre del Greco", "Napoli", "Campania", "Italia"),
      PartialAddress("Via Gaetano Amati", "Venaria Reale", "Torino", "Piemonte", "Italia"),
      PartialAddress("Via Amerigo Vespucci", "Cesano Boscone", "Milano", "Lombardia", "Italia"),
      PartialAddress("Via Carlo Forlanini", "Desio", "Monza e della Brianza", "Lombardia", "Italia"),
      PartialAddress("Via Marino Marini", "Pistoia", "Pistoia", "Toscana", "Italia"),
      PartialAddress("Via per Tornavento", "Lonate Pozzolo", "Varese", "Lombardia", "Italia"),
      PartialAddress("Via Marchese di Villabianca", "Palermo", "Palermo", "Sicilia", "Italia"),
      PartialAddress("Via Nazionale", "Valdidentro", "Sondrio", "Lombardia", "Italia"),
      PartialAddress("Viale Porta Po", "Rovigo", "Rovigo", "Veneto", "Italia")
    )

    assert(RGAddresses.map(_.city) == realAddresses.map(_.city))
    assert(RGAddresses.map(_.county) == realAddresses.map(_.county))
    assert(RGAddresses.map(_.region) == realAddresses.map(_.region))
    assert(RGAddresses.map(_.country) == realAddresses.map(_.country))

    assert(RGAddresses.map(_.street) == realAddresses.map(_.street))

  }


}
