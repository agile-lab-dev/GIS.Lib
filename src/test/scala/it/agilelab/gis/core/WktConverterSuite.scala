package it.agilelab.gis.core

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTWriter
import it.agilelab.gis.core.utils.WktConverter
import org.scalatest.{ FlatSpec, Matchers }

class WktConverterSuite extends FlatSpec with Matchers {

  private val writer: WKTWriter = new WKTWriter()

  "WKT Converter" should "return the Geometry object Point from a wkt String" in {
    val wktString: String = "POINT(1.3456 2.3647)"
    val result = WktConverter.wktConverter(wktString)
    result.get.getGeometryType shouldBe "Point"
    result.get.getCoordinates.sameElements(Array(new Coordinate(1.3456, 2.3647))) shouldBe true
  }

  "WKT Converter" should "return the Geometry object Polygon from a wkt String" in {
    val wktString: String =
      "POLYGON((-96.86593607068062 33.10046566345321,-96.69763147830963 32.47650216763746,-96.06355056166647 " +
        "32.66805499909043,-96.34569615125656 33.201673946909466,-96.86593607068062 33.10046566345321))"
    val result = WktConverter.wktConverter(wktString)
    result.get.getGeometryType shouldBe "Polygon"
  }
}
