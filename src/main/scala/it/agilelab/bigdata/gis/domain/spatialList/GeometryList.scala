/**
 * FILE: LineStringRDD.java
 * PATH: org.datasyslab.geospark.spatialRDD.LineStringRDD.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.domain.spatialList

import java.io.FileWriter

import com.vividsolutions.jts.geom.Geometry
import org.wololo.jts2geojson.GeoJSONWriter

// TODO: Auto-generated Javadoc

/**
 * The Class LineStringRDD.
 */
class GeometryList[T <: Geometry](polygonList: List[T]) extends SpatialList{

  rawSpatialCollection = polygonList
  boundary()
  totalNumberOfRecords = this.rawSpatialCollection.size

  /**
   * Save as geo JSON.
   *
   * @param outputLocation the output location
   */
  def saveAsGeoJSON(outputLocation: String ) {
    val writer = new GeoJSONWriter()
    val fw = new FileWriter(outputLocation, true)

    try{
      rawSpatialCollection.foreach(spatialObject => {
        val json = writer.write(spatialObject.asInstanceOf[Geometry])
        val jsonstring = json.toString
        fw.write(jsonstring)
      })
    }
    finally fw.close()
  }

  /**
   * Minimum bounding rectangle.
   *
   * @return the rectangle RDD
   */
  def MinimumBoundingRectangle(): RectangleList = {

      val rectangleList = this.rawSpatialCollection.map(spatialObject => {
          spatialObject.asInstanceOf[Geometry].getEnvelopeInternal
      })
      new RectangleList(rectangleList)

  }
}
