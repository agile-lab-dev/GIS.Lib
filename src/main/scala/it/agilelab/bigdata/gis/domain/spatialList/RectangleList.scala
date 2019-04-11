/**
 * FILE: RectangleRDD.java
 * PATH: org.datasyslab.geospark.spatialRDD.RectangleRDD.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.domain.spatialList

import com.vividsolutions.jts.geom.Envelope

/**
 * The Class RectangleRDD.
 */

class RectangleList(rawData: List[Envelope]) extends SpatialList {

	rawSpatialCollection = rawData
	boundary()
	totalNumberOfRecords = this.rawSpatialCollection.size

}
