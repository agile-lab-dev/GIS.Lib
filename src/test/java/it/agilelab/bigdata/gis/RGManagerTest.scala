package it.agilelab.bigdata.gis

import it.agilelab.bigdata.gis.loader.{RGManager, RGManagerV2}
import it.agilelab.bigdata.gis.models.PartialAddress
import org.scalatest.FunSuite

class RGManagerTest extends FunSuite{

  val path = "/home/stefano/IdeaProjects/Agile.GIS.Server/data"

  RGManager.init(path)

  test("General test"){

    val answer = RGManager.reverseGeocode(39.832223, 18.341623)

    assert(!answer.city.isEmpty && answer.street.isEmpty)

  }

  test("Small polygon fine-tuning"){

    val answer = RGManager.reverseGeocode(39.832931, 18.341869)

    println(answer.city)
    println(answer.street)

    assert(!answer.city.isEmpty && !answer.street.isEmpty)

  }

  test("Filtering non useful places"){

    val answer = RGManager.reverseGeocode(44.402321, 8.953425)

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

    val RGAddresses: Seq[PartialAddress] = points.map(x => RGManager.reverseGeocode(x._1, x._2))

    val realAddresses = Seq(PartialAddress("Via Lungomare Anglona", "SÃ²ssu/Sorso"),
      PartialAddress("Via Roma","Udine"),
      PartialAddress("Via Muri Bianchi", "Cittadella"),
      PartialAddress("Contrada Forche", "Alcamo"),
      PartialAddress("Viale 16 Aprile", "Poggio a Caiano"),
      PartialAddress("Via Giuseppe Garibaldi", "Lecce"),
      PartialAddress("Corso Trapani", "Torino"),
      PartialAddress("Via Casilina Nord", "Cassino"),
      PartialAddress("Via 4 Novembre", "Merate"),
      PartialAddress("Via Madonna del Salvatore", "Arluno"),
      PartialAddress("Via delle Mimose", "Torre del Greco"),
      PartialAddress("Via Gaetano Amati", "Venaria Reale"),
      PartialAddress("Via Amerigo Vespucci", "Cesano Boscone"),
      PartialAddress("Via Carlo Forlanini", "Desio"),
      PartialAddress("Via Marino Marini", "Pistoia"),
      PartialAddress("SP52", "Cardano al Campo"),
      PartialAddress("Viale Marchese di Villabianca", "Palermo"),
      PartialAddress("Via Nazionale", "Schlanders - Silandro"),
      PartialAddress("Viale Porta Po", "Rovigo")
    )

    assert(RGAddresses.map(_.city) == realAddresses.map(_.city))


  }


}
