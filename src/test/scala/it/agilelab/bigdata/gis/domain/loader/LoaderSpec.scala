package it.agilelab.bigdata.gis.domain.loader

import java.util
import java.util.Random

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.models.OSMStreet
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConversions

class LoaderSpec extends FlatSpec with Matchers with Logger{

  "CTLLoader" should "load" in {
    val str1 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74409; 45,1; 7,74276; 45,10033; 7,74204; 45,10047; 7,74136; 45,10076; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))"
    val str2 = "(2002; 8307; (; ; ); (1; 2; 1; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ); (7,74724; 45,09953; 7,74693; 45,09968; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ; ))"

    val fields1: Array[String] = new CTLLoader(geometryPosition = 7).parseGeometry(str1)
    val fields2: Array[String] = new CTLLoader(geometryPosition = 7).parseGeometry(str2)

    assert(fields1(0) == "2002")
    assert(fields1(1) == "8307")
    assert(fields1(105) == "7,74409")
    assert(fields1(106) == "45,1")
    assert(fields1(107) == "7,74276")
    assert(fields1(108) == "45,10033")

    assert(fields2(105) == "7,74724")
    assert(fields2(106) == "45,09953")

  }

}
