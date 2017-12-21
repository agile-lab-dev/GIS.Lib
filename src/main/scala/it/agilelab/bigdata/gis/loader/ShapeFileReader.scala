package it.agilelab.bigdata.gis.loader

import org.geotools.data.simple._
import org.opengis.feature.simple._
import org.geotools.data.shapefile._
import com.vividsolutions.jts.{geom => jts}
import java.net.URL
import java.io.File
import java.util

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



  def readPointFeatures(path: String): Seq[jts.Point] =

    readSimpleFeatures(path)

      .flatMap { ft => ft.geom[jts.Point] }



  def readLineFeatures(path: String): Seq[jts.LineString] =

    readSimpleFeatures(path)

      .flatMap { ft => ft.geom[jts.LineString] }


  /*
    def readLineFeatures[D](path: String, dataField: String): Seq[LineFeature[D]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.LineString].map(LineFeature(_, ft.attribute[D](dataField))) }
  */


  def readPolygonFeatures(path: String): Seq[jts.Polygon] =

    readSimpleFeatures(path)

      .flatMap { ft => ft.geom[jts.Polygon] }


  /*
    def readPolygonFeatures[D](path: String, dataField: String): Seq[PolygonFeature[D]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.Polygon].map(PolygonFeature(_, ft.attribute[D](dataField))) }
  */

  /*
    def readMultiPointFeatures(path: String): Seq[MultiPointFeature[Map[String,Object]]] =

      readSimpleFeatures(path)

        .flatMap { ft => ft.geom[jts.MultiPoint].map(MultiPointFeature(_, ft.attributeMap)) }



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
  }
  /*
      def readMultiPolygonFeatures[D](path: String, dataField: String): Seq[MultiPolygonFeature[D]] =

        readSimpleFeatures(path)

          .flatMap { ft => ft.geom[jts.MultiPolygon].map(MultiPolygonFeature(_, ft.attribute[D](dataField))) }
    */
}
