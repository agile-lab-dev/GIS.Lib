package it.agilelab.bigdata.gis

import java.sql.Timestamp

import it.agilelab.bigdata.gis.loader.RGManager
import it.agilelab.bigdata.gis.models.PartialAddress
import org.scalatest.FunSuite

class ReverseGeocodeTest extends FunSuite{

  val path = "/home/stefano/Documents/IntesaSmartCareCore/maps"

  RGManager.init(path)

  test("General test on 20 address"){

  val positions = Seq((40.913693, 8.704525),
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

  val addresses = Seq(PartialAddress("Via Lungomare Anglona", "Castelsardo"),
    PartialAddress("Via Roma","Pavia di Udine"),
    PartialAddress("Via Muri Bianchi", "Cittadella"),
    PartialAddress("Contrada Forche", "Alcamo"),
    PartialAddress("Viale 16 Aprile", "Prato"),
    PartialAddress("Via Giuseppe Garibaldi", "Lecce"),
    PartialAddress("Corso Trapani", "Torino"),
    PartialAddress("Via Casilina Nord", "Cassino"),
    PartialAddress("Via 4 Novembre", "Merate"),
    PartialAddress("Via Madonna del Salvatore", "Vittuone"),
    PartialAddress("Via delle Mimose", "Torre del Greco"),
    PartialAddress("Via Gaetano Amati", "Venaria Reale"),
    PartialAddress("Via Amerigo Vespucci", "Cesano Boscone"),
    PartialAddress("Via Carlo Forlanini", "Desio"),
    PartialAddress("Via Marino Marini", "Pistoia"),
    PartialAddress("SP52", "Vizzola Ticino"),
    PartialAddress("Viale Marchese di Villabianca", "Palermo"),
    PartialAddress("Via Nazionale", "Valdidentro"),
    PartialAddress("Viale Porta Po", "Rovigo")
  )


  assert(positions.map(x => RGManager.reverseGeocode(x._1, x._2).city) == addresses.map(_.city))



}


}
