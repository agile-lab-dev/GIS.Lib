package it.agilelab.gis.core.apps

import com.typesafe.config.{ ConfigFactory, ConfigList }
import com.vividsolutions.jts.geom.MultiPolygon
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.loader.ShapeFileReader
import org.geotools.data.{ DataUtilities, DefaultTransaction }
import org.geotools.data.shapefile.{ ShapefileDataStore, ShapefileDataStoreFactory }
import org.geotools.data.simple.{ SimpleFeatureCollection, SimpleFeatureStore }
import org.geotools.feature.simple.SimpleFeatureTypeBuilder
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.opengis.feature.simple.{ SimpleFeature, SimpleFeatureType }
import it.agilelab.gis.core.utils.ScalaUtils._

import java.io.File
import java.util
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.parallel.ParSeq

object MapMerger extends Logger {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    val path: ConfigList = config.getList("osm.index.input_paths")
    val Al8Path = if (path.size > 0) path.head.unwrapped + "/italy/italy-AL8.shp" else ""
    val postalCodePath = if (path.size > 0) path.head.unwrapped + "/italy/italy-postalcodes.shp" else ""

    val cap: Map[String, String] = config.getMap("osm.cap").mapValues(_.asInstanceOf[String])

    val AL8Features: Seq[(MultiPolygon, SimpleFeature)] = ShapeFileReader.readMultiPolygonFeatures(Al8Path)

    val postalCodeFeatures: Seq[(MultiPolygon, SimpleFeature)] =
      ShapeFileReader.readMultiPolygonFeatures(postalCodePath)

    val merged: Seq[(MultiPolygon, SimpleFeature)] = mergeBoundaries(postalCodeFeatures, AL8Features, cap)

    logger.info("Collections merged")
    // extracting simplefeatures
    val collection: Seq[SimpleFeature] = merged.map { case (a, b) => b }
    val featureCollection: SimpleFeatureCollection = DataUtilities.collection(collection.to[Array])

    val outFile = new File(config.getString("osm.index.output_file"))

    val shapeFactory = new ShapefileDataStoreFactory
    val params = new util.HashMap[String, java.io.Serializable]()
    params.put("url", outFile.toURI.toURL)
    params.put("create spatial index", true)

    val dataStore: ShapefileDataStore = shapeFactory.createNewDataStore(params).asInstanceOf[ShapefileDataStore]
    dataStore.createSchema(featureCollection.getSchema)
    dataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84)

    val transaction = new DefaultTransaction("create")
    val typeName = dataStore.getTypeNames()(0)
    val featureSource = dataStore.getFeatureSource(typeName)
    featureSource match {
      case featureStore: SimpleFeatureStore =>
        featureStore.setTransaction(transaction)
        try {
          featureStore.addFeatures(featureCollection)
          transaction.commit
        } catch {
          case ex: Exception =>
            ex.printStackTrace()
            transaction.rollback()
        }
        transaction.close
      case _ => transaction.close
    }

  }

  def mergeBoundaries(
      inner: Seq[(MultiPolygon, SimpleFeature)],
      outer: Seq[(MultiPolygon, SimpleFeature)],
      cap: Map[String, String]
  ): Seq[(MultiPolygon, SimpleFeature)] = {

    val featureType = outer(0)._2.getFeatureType
    val builder = new SimpleFeatureTypeBuilder()
    builder.setName(featureType.getName)
    builder.setSuperType(featureType.getSuper.asInstanceOf[SimpleFeatureType])
    builder.addAll(featureType.getAttributeDescriptors)
    builder.add("cap", classOf[String])
    val newFeatureType = builder.buildFeatureType

    val innerPar = inner.par
    outer.par
      .map(b => (b, innerPar.filter(_._1.getInteriorPoint.coveredBy(b._1))))
      .flatMap { case (out, inners) =>
        if (inners.isEmpty) {
          val capValue = cap.getOrElse(out._2.getAttribute("name").toString, None)
          val feature = DataUtilities.reType(newFeatureType, out._2)
          feature.setAttribute("cap", capValue)
          logger.info("Adding cap to {}", out._2.getAttribute("name"))
          val tuple: (MultiPolygon, SimpleFeature) = (out._1, feature)
          Seq(tuple)
        } else {
          val a: ParSeq[(MultiPolygon, SimpleFeature)] = inners.map { inner =>
            val cap: String = inner._2.getAttribute("CAP").toString
            val feature = DataUtilities.reType(newFeatureType, out._2)
            feature.setAttribute("cap", cap)
            (out._1, feature)
          }
          a
        }
      }
      .seq
  }

}
