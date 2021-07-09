/** FILE: LineStringRDD.java
  * PATH: org.datasyslab.geospark.spatialRDD.LineStringRDD.java
  * Copyright (c) 2017 Arizona State University Data Systems Lab
  * All rights reserved.
  */
package it.agilelab.gis.domain.spatialList

import com.vividsolutions.jts.geom._
import org.wololo.jts2geojson.GeoJSONWriter

import java.io._

/** The Class LineStringRDD.
  */
class StreetList(polygonList: List[LineString]) extends SpatialList {

  rawSpatialCollection = polygonList
  totalNumberOfRecords = this.rawSpatialCollection.size

  boundary()

  /** Save as geo JSON.
    *
    * @param outputLocation the output location
    */
  def saveAsGeoJSON(outputLocation: String) {

    val writer = new GeoJSONWriter()
    val fw = new FileWriter(outputLocation, true)

    try rawSpatialCollection.foreach { spatialObject =>
      val json = writer.write(spatialObject.asInstanceOf[Geometry]).toString
      fw.write(json)
    } finally fw.close()

  }

  /** Minimum bounding rectangle.
    *
    * @return the rectangle RDD
    */
  def MinimumBoundingRectangle(): RectangleList = {

    val rectangleList = this.rawSpatialCollection.map { spatialObject =>
      spatialObject.asInstanceOf[Geometry].getEnvelopeInternal
    }
    new RectangleList(rectangleList)

  }

}
