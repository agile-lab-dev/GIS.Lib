package it.agilelab.gis.domain.loader

import com.typesafe.config.{ Config, ConfigFactory }
import it.agilelab.gis.core.utils.ConfigurationProperties.POI
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.managers.PoiManager
import org.scalatest.{ FlatSpec, Matchers }

class PoiManagerSpec extends FlatSpec with Matchers {

  val conf: Config = ConfigFactory.load()
  val poiConf: Config = conf.getConfig(POI.value)
  val manager: PoiManager = PoiManager(poiConf)

  "A specific point" should "have amenity around" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 41.8664062, 12.4859085, None, System.currentTimeMillis())
    val res = manager.findAmenity(point, 100)
    res match {
      case Right(result) =>
        result.size should be > 0
        result.map(i => i.geometry.amenity) should contain allOf (Some("bank"), Some("atm"), Some("recycling"), Some(
          "post_office"), Some("post_box"), Some("telephone"))
      case Left(err) => fail(err.ex)
    }

  }

  "findAmenity" should "return an empty list if there are no points of interest of type amenity nearby" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 42.9671456, 11.4423778, None, System.currentTimeMillis())
    val res = manager.findAmenity(point, 100)
    res match {
      case Right(result) =>
        result.isEmpty should be(true)
      case Left(err) => fail(err.ex)
    }

  }

  "A specific point" should "have landuse around" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 41.8653953, 12.4869972, None, System.currentTimeMillis())
    val res = manager.findLanduse(point, 100)
    res match {
      case Right(result) =>
        result.size should be > 0
        result.map(i => i.geometry.landuse) should contain(Some("residential"))
      case Left(err) => fail(err.ex)
    }

  }

  "findLanduse" should "return an empty list if there are no points of interest of type landuse nearby" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 43.1922062, 11.4746867, None, System.currentTimeMillis())
    val res = manager.findLanduse(point, 100)
    res match {
      case Right(result) =>
        result.isEmpty should be(true)
      case Left(err) => fail(err.ex)
    }

  }

  "A specific point" should "have leisure around" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 41.86483111, 12.48439686, None, System.currentTimeMillis())
    val res = manager.findLeisure(point, 100)
    res match {
      case Right(result) =>
        result.size should be > 0
        result.map(i => i.geometry.leisure) should contain(Some("park"))
      case Left(err) => fail(err.ex)
    }

  }

  "findLeisure" should "return an empty list if there are no points of interest of type leisure nearby" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 42.7723063, 12.2366949, None, System.currentTimeMillis())
    val res = manager.findLeisure(point, 100)
    res match {
      case Right(result) =>
        result.isEmpty should be(true)
      case Left(err) => fail(err.ex)
    }

  }

  "A specific point" should "have natural around" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 41.86400746, 12.47893681, None, System.currentTimeMillis())
    val res = manager.findNatural(point, 100)

    res match {
      case Right(result) =>
        result.size should be > 0
        result.map(i => i.geometry.natural) should contain(Some("tree"))
      case Left(err) => fail(err.ex)
    }

  }

  "findNatural" should "return an empty list if there are no points of interest of type natural nearby" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 43.2872833, 11.7979953, None, System.currentTimeMillis())
    val res = manager.findNatural(point, 100)
    res match {
      case Right(result) =>
        result.isEmpty should be(true)
      case Left(err) => fail(err.ex)
    }

  }

  "A specific point" should "have shop around" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 41.85631609, 12.48107449, None, System.currentTimeMillis())
    val res = manager.findShop(point, 100)
    res match {
      case Right(result) =>
        result.size should be > 0
        result.map(i => i.geometry.shop) should contain allOf (Some("shoes"), Some("vacant"), Some("furniture"), Some(
          "tobacco"), Some("pastry"))
      case Left(err) => fail(err.ex)
    }

  }

  "findShop" should "return an empty list if there are no points of interest of type shop nearby" in {

    val id = "abc"
    val point = IdentifiableGPSPoint(id, 42.4055667, 11.7607662, None, System.currentTimeMillis())
    val res = manager.findShop(point, 100)
    res match {
      case Right(result) =>
        result.isEmpty should be(true)
      case Left(err) => fail(err.ex)
    }

  }

}
