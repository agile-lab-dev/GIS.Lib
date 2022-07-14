/** FILE: LineStringRDD.java
  * PATH: org.datasyslab.geospark.spatialRDD.LineStringRDD.java
  * Copyright (c) 2017 Arizona State University Data Systems Lab
  * All rights reserved.
  */
package it.agilelab.gis.domain.spatialList

import com.vividsolutions.jts.geom.Geometry
import org.wololo.jts2geojson.GeoJSONWriter

import java.io.FileWriter

// TODO: Auto-generated Javadoc

/** The Class LineStringRDD.
  */
class GeometryList[T <: Geometry](polygonList: List[T]) extends SpatialList {

  rawSpatialCollection = polygonList
  boundary()
  totalNumberOfRecords = this.rawSpatialCollection.size

  /** Save as geo JSON.
    *
    * @param outputLocation the output location
    */
  def saveAsGeoJSON(outputLocation: String): Unit = {
    val writer = new GeoJSONWriter()
    val fw = new FileWriter(outputLocation, true)

    try rawSpatialCollection.foreach { spatialObject =>
      val json = writer.write(spatialObject.asInstanceOf[Geometry]).toString
      fw.write(json)
    } finally fw.close()
  }

}
