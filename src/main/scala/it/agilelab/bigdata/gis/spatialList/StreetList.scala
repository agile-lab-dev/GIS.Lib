/**
 * FILE: LineStringRDD.java
 * PATH: org.datasyslab.geospark.spatialRDD.LineStringRDD.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.spatialList

import java.io._
import java.util.ArrayList
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.vividsolutions.jts.geom._
import org.wololo.jts2geojson.GeoJSONWriter

// TODO: Auto-generated Javadoc




/**
 * The Class LineStringRDD.
 */
class StreetList(polygonList: List[LineString]) extends SpatialList{

    def this(lineList: ArrayList[Geometry]) = this( lineList.toArray().map(s => s.asInstanceOf[LineString]).toList )

		this.rawSpatialCollection = polygonList
    this.boundary();
    this.totalNumberOfRecords = this.rawSpatialCollection.size;




    /**
     * Save as geo JSON.
     *
     * @param outputLocation the output location
     */
    def saveAsGeoJSON(outputLocation: String ) {
        val writer = new GeoJSONWriter();
        val fw = new FileWriter(outputLocation, true)
        try{
        this.rawSpatialCollection.foreach(spatialObject => {
            val json = writer.write(spatialObject.asInstanceOf[Geometry])
            val jsonstring = json.toString()
            fw.write(jsonstring)
        })}
        finally fw.close()
    }
    
    /**
     * Minimum bounding rectangle.
     *
     * @return the rectangle RDD
     */
    def MinimumBoundingRectangle(): RectangleList = {
        val rectangleList = this.rawSpatialCollection.map(spatialObject => {
            spatialObject.asInstanceOf[Geometry].getEnvelopeInternal()
        })
        new RectangleList(rectangleList);
    }
}
