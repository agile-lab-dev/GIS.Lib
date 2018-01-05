package it.agilelab.bigdata.gis.loader

import org.geotools.data.simple._
import org.opengis.feature.simple._
import org.geotools.data.shapefile._
import com.vividsolutions.jts.{geom => jts}
import java.net.URL
import java.io.File
import java.util

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, MultiPolygon, Point, Polygon}

import scala.collection.mutable
import scala.collection.JavaConversions._

object ShapeFileReader {

  implicit class SimpleFeatureWrapper(ft: SimpleFeature) {

    def geom[G <: jts.Geometry: Manifest]: Option[G] =

      ft.getAttribute(0) match {

        case g: G => Some(g)
        case _ => None

      }



    def attributeMap: Map[String, Object] =

      ft.getProperties.drop(1).map { p =>

        (p.getName.toString, ft.getAttribute(p.getName))

      }.toMap



    def attribute[D](name: String): D =

      ft.getAttribute(name).asInstanceOf[D]

  }



  def readSimpleFeatures(path: String) = {

    // Extract the features as GeoTools 'SimpleFeatures'

    val url = s"file://${new File(path).getAbsolutePath}"

    val ds: ShapefileDataStore = new ShapefileDataStore(new URL(url))

    val ftItr: SimpleFeatureIterator = ds.getFeatureSource.getFeatures.features



    try {

      val simpleFeatures = mutable.ListBuffer[SimpleFeature]()

      while(ftItr.hasNext) simpleFeatures += ftItr.next()

      simpleFeatures.toList

    } finally {

      ftItr.close

      ds.dispose

    }

  }



  def readPointFeatures(path: String): Seq[(jts.Point, util.List[AnyRef])] = {

    readSimpleFeatures(path)
      .flatMap { ft => ft.geom[jts.Point].map(e => (e, ft.getAttributes)) }
  }

  def readPointFeaturesToPolygon(path: String): Seq[(jts.Polygon, util.List[AnyRef])] = {

    val points: Seq[(Point, util.List[AnyRef])] = readSimpleFeatures(path)
      .flatMap { ft => ft.geom[jts.Point].map(e => (e, ft.getAttributes)) }

    val fact = new GeometryFactory()

    points.map{ x => {
      val coordinate = x._1.getCoordinate
      val newLineRing = Array(new Coordinate(coordinate.x - 0.001, coordinate.y, coordinate.z),
        new Coordinate(coordinate.x, coordinate.y - 0.001, coordinate.z),
        new Coordinate(coordinate.x + 0.001, coordinate.y, coordinate.z),
        new Coordinate(coordinate.x, coordinate.y + 0.001, coordinate.z),
        new Coordinate(coordinate.x - 0.001, coordinate.y, coordinate.z) )

      (fact.createPolygon(newLineRing), x._2)
      }
    }.filter(x => !x._2(3).toString.equals("suburb") && !x._2(3).toString.equals("farm") && !x._2(3).toString.equals("locality") && !x._2(3).toString.equals("island") && !x._2(3).toString.equals("hamlet") && !x._2(3).toString.equals("region") && !x._2(3).toString.equals("village"))

  }


  def readLineFeatures(path: String): Seq[jts.LineString] =

    readSimpleFeatures(path)

      .flatMap { ft => ft.geom[jts.LineString] }


  /*
    def readLineFeatures[D](path: String, dataField: String): Seq[LineFeature[D]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.LineString].map(LineFeature(_, ft.attribute[D](dataField))) }
  */


  def readPolygonFeatures(path: String): Seq[(jts.Polygon, util.List[AnyRef])] =

    readSimpleFeatures(path)

      .flatMap { ft => ft.geom[jts.Polygon].map(e => (e, ft.getAttributes)) }


  /*
    def readPolygonFeatures[D](path: String, dataField: String): Seq[PolygonFeature[D]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.Polygon].map(PolygonFeature(_, ft.attribute[D](dataField))) }
  */


    def readMultiPointFeatures(path: String): Seq[(jts.MultiPoint, util.List[AnyRef])] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.MultiPoint]
          .map(e => (e, ft.getAttributes)) }


  /*
    def readMultiPointFeatures[D](path: String, dataField: String): Seq[MultiPointFeature[D]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.MultiPoint].map(MultiPointFeature(_, ft.attribute[D](dataField))) }


  */
  def readMultiLineFeatures(path: String): Seq[(jts.MultiLineString, util.List[AnyRef])] = {

    readSimpleFeatures(path)
      .flatMap { ft => ft.geom[jts.MultiLineString]
      .map(e => (e, ft.getAttributes)) }

  }

  /*

    def readMultiLineFeatures[D](path: String, dataField: String): Seq[MultiLineFeature[D]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.MultiLineString].map(MultiLineFeature(_, ft.attribute[D](dataField))) }

*/

  def readMultiPolygonFeatures(path: String): Seq[(jts.MultiPolygon, util.List[AnyRef])] = {

   readSimpleFeatures(path)
      .flatMap { ft => ft.geom[jts.MultiPolygon].map(mp => (mp, ft.getAttributes)) }
      .filter(x => !x._2(3).toString.equals("suburb") && !x._2(3).toString.equals("farm") && !x._2(3).toString.equals("locality") && !x._2(3).toString.equals("island") && !x._2(3).toString.equals("hamlet") && !x._2(3).toString.equals("region") && !x._2(3).toString.equals("village"))

  }
  /*
      def readMultiPolygonFeatures[D](path: String, dataField: String): Seq[MultiPolygonFeature[D]] =

        readSimpleFeatures(path)

          .flatMap { ft => ft.geom[jts.MultiPolygon].map(MultiPolygonFeature(_, ft.attribute[D](dataField))) }
    */
}
