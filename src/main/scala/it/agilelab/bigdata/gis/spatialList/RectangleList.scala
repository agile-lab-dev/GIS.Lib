/**
 * FILE: RectangleRDD.java
 * PATH: org.datasyslab.geospark.spatialRDD.RectangleRDD.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.spatialList

import com.vividsolutions.jts.geom.Envelope


// TODO: Auto-generated Javadoc

/**
 * The Class RectangleRDD.
 */

class RectangleList(rawData: List[Envelope]) extends SpatialList {


	this.rawSpatialCollection = rawData
	this.boundary();
	this.totalNumberOfRecords = this.rawSpatialCollection.size

}
