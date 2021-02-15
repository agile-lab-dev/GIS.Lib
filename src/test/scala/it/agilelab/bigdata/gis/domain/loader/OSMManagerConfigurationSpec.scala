package it.agilelab.bigdata.gis.domain.loader

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.{Success, Try}

class OSMManagerConfigurationSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "osm manager configuration reading" should "work" in {

    val conf: Config = ConfigFactory.load()
    val osmConf = Try{OSMManagerConfiguration(conf)}

    osmConf shouldBe a[Success[_]]

  }

}

