package it.agilelab.gis.domain.loader

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory, MultiPolygon, Point }
import com.vividsolutions.jts.{ geom => jts }
import org.geotools.data.shapefile._
import org.geotools.data.simple._
import org.opengis.feature.simple._

import java.io.File
import java.net.URL
import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object ShapeFileReader {

  implicit class SimpleFeatureWrapper(ft: SimpleFeature) {

    def geom[G <: jts.Geometry: Manifest]: Option[G] = geom(0)

    def geom[G <: jts.Geometry: Manifest](idx: Int): Option[G] =
      ft.getAttribute(idx) match {
        case g: G => Some(g)
        case _    => None
      }

    def geom[G <: jts.Geometry: Manifest](attribute: String): Option[G] =
      ft.getAttribute(attribute) match {
        case g: G => Some(g)
        case _    => None
      }

    def attributeMap: Map[String, Object] =
      ft.getProperties
        .drop(1)
        .map { p =>
          (p.getName.toString, ft.getAttribute(p.getName))
        }
        .toMap

    def attribute[D](name: String): D = ft.getAttribute(name).asInstanceOf[D]
  }

  def readSimpleFeatures(path: String): Seq[SimpleFeature] = {

    // Extract the features as GeoTools 'SimpleFeatures'
    val url = s"file://${new File(path).getAbsolutePath}"
    val ds: ShapefileDataStore = new ShapefileDataStore(new URL(url))
    val ftItr: SimpleFeatureIterator = ds.getFeatureSource.getFeatures.features

    try {
      val simpleFeatures = ListBuffer[SimpleFeature]()
      while (ftItr.hasNext) simpleFeatures += ftItr.next()
      simpleFeatures.toList
    } finally {
      ftItr.close()
      ds.dispose()
    }
  }

  def readPointFeatures(path: String): Seq[(jts.Point, util.List[AnyRef])] =
    readSimpleFeatures(path).flatMap(ft => ft.geom[jts.Point].map(e => (e, ft.getAttributes)))

  def readPointFeaturesToPolygon(path: String): Seq[(jts.Polygon, util.List[AnyRef])] = {

    val points: Seq[(Point, util.List[AnyRef])] = readSimpleFeatures(path)
      .flatMap(ft => ft.geom[jts.Point].map(e => (e, ft.getAttributes)))

    val fact = new GeometryFactory()

    points.map { x =>
      val coordinate = x._1.getCoordinate
      val newLineRing = Array(
        new Coordinate(coordinate.x - 0.001, coordinate.y, coordinate.z),
        new Coordinate(coordinate.x, coordinate.y - 0.001, coordinate.z),
        new Coordinate(coordinate.x + 0.001, coordinate.y, coordinate.z),
        new Coordinate(coordinate.x, coordinate.y + 0.001, coordinate.z),
        new Coordinate(coordinate.x - 0.001, coordinate.y, coordinate.z)
      )

      (fact.createPolygon(newLineRing), x._2)
    }

  }

  def readLineFeatures(path: String): Seq[jts.LineString] =
    readSimpleFeatures(path).flatMap(ft => ft.geom[jts.LineString])

  def readPolygonFeatures(path: String, attr: String): Seq[(jts.Polygon, SimpleFeature)] =
    readSimpleFeatures(path)
      .flatMap(ft => ft.geom[jts.Polygon](attr).map(e => (e, ft)))

  def readMultiPointFeatures(path: String): Seq[(jts.MultiPoint, util.List[AnyRef])] =
    readSimpleFeatures(path)
      .flatMap { ft =>
        ft.geom[jts.MultiPoint]
          .map(e => (e, ft.getAttributes))
      }

  def readMultiLineFeatures(path: String): Seq[(jts.MultiLineString, util.List[AnyRef])] =
    readSimpleFeatures(path).flatMap { ft =>
      ft.geom[jts.MultiLineString].map(e => (e, ft.getAttributes))
    }

  def readMultiPolygonFeatures(path: String, attr: String = "the_geom"): Seq[(MultiPolygon, SimpleFeature)] =
    readSimpleFeatures(path)
      .map(ft => (ft.getAttribute(attr).asInstanceOf[MultiPolygon], ft))

}
