package it.agilelab.bigdata.gis

import it.agilelab.bigdata.gis.loader.RGManager
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


}
