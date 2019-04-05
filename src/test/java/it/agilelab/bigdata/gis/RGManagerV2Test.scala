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

    assert(answer.city.isDefined && answer.street.isDefined)

  }

  test("General report validation") {

    val points = Seq(
      (37.496343, 15.067200),
      (38.128873, 13.359253),
      (37.310851, 13.583665),
      (38.064311, 15.621957),
      (40.351657, 18.175206),
      (40.666426, 16.605022)
    )

    val result: Seq[Address] = points.map(x => ReverseGeocodingManager.reverseGeocode(x._1, x._2, true))

    val realAddresses = Seq(
      Address(Some("Lungomare Augusto Ottaviano"), Some(""), Some("Catania"), Some("Sicily"), Some("Italia")),
      Address(Some("Largo dei Greci"), Some(""), Some("Palermo"), Some("Sicily"), Some("Italia")),
      Address(Some("Traversa Paviglianiti"), Some(""), Some("Agrigento"), Some("Sicily"), Some("Italia")),
      Address(None, Some(""), Some("Reggio di Calabria"), Some("Calabria"), Some("Italia")),
      Address(Some("Piazzale dell'Aeronautica"),Some(""),Some("Lecce"),Some("Apulia"),Some("Italia")),
      Address(Some("Via Ascanio Persio"), Some(""), Some("Matera"), Some("Basilicata"), Some("Italia"))
    )

    result.zip(realAddresses).foreach(res => {

      val current = res._1
      val expected = res._2

      println("Current:  " + current)
      println("Expected: " + expected)

      assert(current.street.equals(expected.street))
      assert(current.city.equals(expected.city))
      assert(current.region.equals(expected.region))

    })

  }



}
