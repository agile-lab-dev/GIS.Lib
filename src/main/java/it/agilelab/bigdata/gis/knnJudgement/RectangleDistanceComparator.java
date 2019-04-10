/**
 * FILE: RectangleDistanceComparator.java
 * PATH: org.datasyslab.geospark.knnJudgement.RectangleDistanceComparator.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.knnJudgement;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;
import java.util.Comparator;

// TODO: Auto-generated Javadoc

/**
 * The Class RectangleDistanceComparator.
 */
public class RectangleDistanceComparator implements Comparator<Envelope>, Serializable{
	
	/** The query center. */
	Point queryCenter;
	
	/**
	 * Instantiates a new rectangle distance comparator.
	 *
	 * @param queryCenter the query center
	 */
	public RectangleDistanceComparator(Point queryCenter)
	{
		this.queryCenter=queryCenter;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Envelope p1, Envelope p2) {
		double distance1 = p1.distance(queryCenter.getEnvelopeInternal());
		double distance2 = p2.distance(queryCenter.getEnvelopeInternal());
		if (distance1 > distance2) {
			return 1;
		} else if (distance1 == distance2) {
			return 0;
		}
		return -1;
	}
}
