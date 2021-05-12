package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory, MultiPolygon, Point }
import com.vividsolutions.jts.{ geom => jts }
import org.geotools.data.shapefile._
import org.geotools.data.simple._
import org.opengis.feature.simple._
import org.slf4j.LoggerFactory

import java.io.File
import java.net.URL
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object ShapeFileReader {

  private val logger = LoggerFactory.getLogger(ShapeFileReader.getClass)

  implicit class SimpleFeatureWrapper(ft: SimpleFeature) {

    def geom[G <: jts.Geometry: Manifest]: Option[G] =
      ft.getAttribute(0) match {
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

    def attribute[D](name: String): D =
      ft.getAttribute(name).asInstanceOf[D]
  }

  def readSimpleFeatures(path: String): Seq[SimpleFeature] = {

    // Extract the features as GeoTools 'SimpleFeatures'
    val url = s"file://${new File(path).getAbsolutePath}"
    val ds: ShapefileDataStore = new ShapefileDataStore(new URL(url))
    val ftItr: SimpleFeatureIterator = ds.getFeatureSource.getFeatures.features

    val simpleFeatures = ListBuffer[SimpleFeature]()
    try while (ftItr.hasNext) simpleFeatures += ftItr.next()
    catch {
      case e: Exception => logger.warn(s"Exception occurred - num features read so far ${simpleFeatures.size}", e)
    } finally {
      ftItr.close()
      ds.dispose()
    }
    simpleFeatures
  }

  def readPointFeatures(path: String): Seq[(jts.Point, SimpleFeature)] =
    readSimpleFeatures(path).flatMap(ft => ft.geom[jts.Point].map(e => (e, ft)))

  def readPointFeaturesToPolygon(path: String): Seq[(jts.Polygon, SimpleFeature)] = {

    val points: Seq[(Point, SimpleFeature)] = readSimpleFeatures(path)
      .flatMap(ft => ft.geom[jts.Point].map(e => (e, ft)))

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

  def readPolygonFeatures(path: String): Seq[(jts.Polygon, SimpleFeature)] =
    readSimpleFeatures(path)
      .flatMap(ft => ft.geom[jts.Polygon].map(e => (e, ft)))

  def readMultiPointFeatures(path: String): Seq[(jts.MultiPoint, SimpleFeature)] =
    readSimpleFeatures(path)
      .flatMap { ft =>
        ft.geom[jts.MultiPoint]
          .map(e => (e, ft))
      }

  def readMultiLineFeatures(path: String): Seq[(jts.MultiLineString, SimpleFeature)] =
    readSimpleFeatures(path).flatMap { ft =>
      ft.geom[jts.MultiLineString].map(e => (e, ft))
    }

  def readMultiPolygonFeatures(path: String): Seq[(MultiPolygon, SimpleFeature)] =
    readSimpleFeatures(path)
      .flatMap(ft => ft.geom[jts.MultiPolygon].map(mp => (mp, ft)))

}
