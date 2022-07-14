package it.agilelab.gis.domain.loader

import java.io.File
import java.nio.file.Paths
import java.util.Date

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory }
import it.agilelab.gis.domain.managers.GeometryMembershipInfoManager
import it.agilelab.gis.domain.models.CategoriesCfg.{ GeoMetadataCfg, Country => CountryCfg, Custom => CustomCfg }
import it.agilelab.gis.domain.models.{ CategoriesCfg, InputCategory }
import org.scalatest.{ FlatSpec, Matchers }

import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

class GeoCategoryMembershipSpec extends FlatSpec with Matchers {

  private val coord_bosnia = new Coordinate(17.57215, 42.92765)

  private val coord_andorra = new Coordinate(1.53402, 42.55658)

  private val coord_spain = new Coordinate(1.66921, 42.50555)

  private val coord_toulouse = new Coordinate(1.648, 43.493)

  "Config for the GeometryMembershipManager" should "be successfully loaded" in {
    val sut = CategoriesCfg
    val result = sut.load
    result shouldBe a[Success[_]]
    result.get.geoDataPath.toString shouldBe ("src/test/resources/osm/categories").replace("/", File.separator)
    result.get.categoryInfo.size shouldEqual 1
    result.get.categoryInfo should contain(
      CountryCfg(
        Map(2 -> GeoMetadataCfg("Code"), 3 -> GeoMetadataCfg("Name")),
        3
      )
    )
  }

  "GeometryMembershipManager" should "not be initialize if categories do not match" in {
    val aWrongPathCfg = CategoriesCfg(
      Paths.get("src/test/resources/osm/aPathNotExisting"),
      Seq(CustomCfg("aLabel", Map.empty, 0))
    )
    val sut = GeometryMembershipInfoManager
    sut(aWrongPathCfg) shouldBe a[Failure[_]]
  }

  "GeometryMembershipManager" should "load Country Category successfully" in {
    val categoriesCfg = CategoriesCfg(
      Paths.get("src/test/resources/osm/categories"),
      Seq(CountryCfg(Map.empty, 0))
    )
    val sut = GeometryMembershipInfoManager
    sut.apply(categoriesCfg) shouldBe a[Success[_]]
  }

  "GeometryMembershipManager.getGeoMembershipInfoOf[Point]" should
    "returns theEmpty list when external point" in {
      val res = for {
        cfg   <- CategoriesCfg.load
        sut   <- GeometryMembershipInfoManager(cfg)
        point <- Try(new GeometryFactory().createPoint(coord_spain))
      } yield (sut.getGeoMembershipInfoOf(InputCategory.Country, point))

      res shouldBe a[Success[_]]
      res.get shouldBe Seq.empty
    }

  "GeometryMembershipManager.getGeoMembershipInfoOf[Point]" should
    "returns the list of matching shapes when internal point - 1" in {
      val res = for {
        cfg   <- CategoriesCfg.load
        sut   <- GeometryMembershipInfoManager(cfg)
        point <- Try(new GeometryFactory().createPoint(coord_andorra))
      } yield (sut.getGeoMembershipInfoOf(InputCategory.Country, point))

      res shouldBe a[Success[_]]
      res.get.size should be(1)
      res.get.head.geometryLabel should be("Andorra_AL2")
    }

  "GeometryMembershipManager.getGeoMembershipInfoOf[Point]" should
    "returns the list of matching shapes when internal point - 2" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        point <- Try(
          new GeometryFactory().createPoint(new Coordinate(18.3884, 44.1812))
        )
      } yield (sut.getGeoMembershipInfoOf(InputCategory.Country, point))

      res shouldBe a[Success[_]]
      res.get.size should be(1)
      res.get.head.geometryLabel should be("Bosnia and Herzegovina")
    }

  "GeometryMembershipManager.getGeoMembershipInfoOf[Point]" should
    "returns the list of matching shapes when multi categories" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        point <- Try(
          new GeometryFactory().createPoint(new Coordinate(7.6756, 45.0735))
        )
      } yield (sut.getGeoMembershipInfoOf(InputCategory.Country, point))

      res shouldBe a[Success[_]]
      res.get.size should be(2)
      val categoryLabelsRes = res.get.map(_.geometryLabel)
      categoryLabelsRes should contain("Turin")
      categoryLabelsRes should contain("Italy")
    }

  "GeometryMembershipManager.getFirstGeoMembershipInfoOf[Point]" should
    "returns None when external point" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        point <- Try(
          new GeometryFactory().createPoint(new Coordinate(1.66921, 42.50555))
        )
      } yield (sut.getFirstGeoMembershipInfoOf(InputCategory.Country, point))

      res shouldBe a[Success[_]]
      res.get shouldBe None
    }

  "GeometryMembershipManager.getFirstGeoMembershipInfoOf[Point]" should
    "returns the first matching category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        point <- Try(
          new GeometryFactory().createPoint(new Coordinate(7.6756, 45.0735))
        )
      } yield (sut.getFirstGeoMembershipInfoOf(InputCategory.Country, point))

      res shouldBe a[Success[_]]
      res.get.size should be(1)
      res.get.map(_.geometryLabel) should (contain("Turin") or contain("Italy"))
    }

  "GeometryMembershipManager.getGeoMembershipInfoOf[MultiPoint]" should
    "returns theEmpty list when no point inside any shapes of the category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        line <- Try(
          new GeometryFactory()
            .createMultiPoint(Seq(coord_spain, coord_toulouse).toArray)
        )
      } yield sut.getGeoMembershipInfoOf(InputCategory.Country, line)

      res shouldBe a[Success[_]]
      res.get shouldBe Seq.empty
    }

  "GeometryMembershipManager.getGeoMembershipInfoOf[MultiPoint]" should
    "returns the list of matching shapes when some points are inside the category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        points <- Try(
          new GeometryFactory().createMultiPoint(
            Seq(coord_andorra, coord_bosnia, coord_bosnia, coord_toulouse).toArray
          )
        )
      } yield sut.getGeoMembershipInfoOf(InputCategory.Country, points)

      res shouldBe a[Success[_]]
      res.get.size shouldBe 2
      res.get.map(_.geometryLabel) should (contain("Andorra_AL2") and contain(
        "Bosnia and Herzegovina"
      ))
    }

  "GeometryMembershipManager.getFirstGeoMembershipInfoOf[MultiPoint]" should
    "returns theEmpty list when no point inside any shapes of the category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        line <- Try(
          new GeometryFactory()
            .createMultiPoint(Seq(coord_spain, coord_toulouse).toArray)
        )
      } yield sut.getGeoMembershipInfoOf(InputCategory.Country, line)

      res shouldBe a[Success[_]]
      res.get shouldBe Seq.empty
    }

  "GeometryMembershipManager.getFirstGeoMembershipInfoOf[MultiPoint]" should
    "returns the list of matching shapes when some points are inside the category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        points <- Try(
          new GeometryFactory().createMultiPoint(
            Seq(coord_andorra, coord_bosnia, coord_toulouse).toArray
          )
        )
      } yield sut.getGeoMembershipInfoOf(InputCategory.Country, points)

      res shouldBe a[Success[_]]
      res.get.size shouldBe 2
      res.get.map(_.geometryLabel) should (contain("Andorra_AL2") or contain(
        "Bosnia and Herzegovina"
      ))
    }

  "GeometryMembershipManager.getFirstGeoMembershipInfoOf[MultiPoint] - STRICT" should
    "returns the list of matching shapes when strict search enabled - some points are inside the category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        points <- Try(
          new GeometryFactory().createMultiPoint(
            Seq(coord_andorra, coord_bosnia, coord_toulouse).toArray
          )
        )
      } yield sut.getGeoMembershipInfoOf(
        InputCategory.Country,
        points,
        strictMembership = true
      )

      res shouldBe a[Success[_]]
      res.get.size shouldBe 0
    }

  "GeometryMembershipManager.getFirstGeoMembershipInfoOf[MultiPoint] - STRICT" should
    "returns the empty list of matching shapes when strict search enabled - all points are inside the category" in {
      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        points <- Try(
          new GeometryFactory()
            .createMultiPoint(Seq(coord_andorra, coord_bosnia).toArray)
        )
      } yield sut.getGeoMembershipInfoOf(
        InputCategory.Country,
        points,
        strictMembership = true
      )

      res shouldBe a[Success[_]]
      res.get.size shouldBe 2
      res.get.map(_.geometryLabel) should (contain("Andorra_AL2") and contain(
        "Bosnia and Herzegovina"
      ))
    }

  "GeometryMembershipManager.getGeoMembershipInfoOf[MultiPoint]" should
    "performs well while benchmarking" in {

      val res = for {
        cfg <- CategoriesCfg.load
        sut <- GeometryMembershipInfoManager(cfg)
        seq = (1 to 1) flatMap (_ =>
          List(
            coord_andorra,
            coord_andorra,
            coord_andorra,
            coord_spain,
            coord_andorra,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia,
            coord_bosnia
          ))
        point <- Try(new GeometryFactory().createMultiPoint(seq.toArray))
        runnable = new Runnable {
          override def run(): Unit = {
            sut.getGeoMembershipInfoOf(
              InputCategory.Country,
              point,
              strictMembership = true
            )
            ()
          }
        }
      } yield runnable

      val value = res.get
      val threads = (1 to 4).map(_ => new Thread(value)) ++ (1 to 4).map(_ => new Thread(value))
      val t0 = System.currentTimeMillis()
      println(new Date().toInstant)
      threads.foreach(_.start())
      threads.foreach(_.join())
      val t1 = System.currentTimeMillis()
      println(new Date().toInstant)
      println(t0 - t1 abs)

    }

}
