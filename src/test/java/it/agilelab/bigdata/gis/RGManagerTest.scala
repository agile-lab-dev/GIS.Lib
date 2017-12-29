package it.agilelab.bigdata.gis

import it.agilelab.bigdata.gis.loader.RGManager
import org.scalatest.FunSuite

class RGManagerTest extends FunSuite{

  val path = "/home/stefano/IdeaProjects/Agile.GIS.Server/data"

  RGManager.init(path)

  val answer = RGManager.reverseGeocode(39.832223, 18.341623)

  assert(!answer.city.isEmpty && answer.street.isEmpty)

}
