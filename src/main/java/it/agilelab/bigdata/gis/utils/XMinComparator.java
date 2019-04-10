/**
 * FILE: XMinComparator.java
 * PATH: org.datasyslab.geospark.utils.XMinComparator.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.utils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import it.agilelab.bigdata.gis.geometryObjects.Circle;

import java.io.Serializable;
import java.util.Comparator;

// TODO: Auto-generated Javadoc

/**
 * The Class XMinComparator.
 */
public class XMinComparator implements Comparator<Object>, Serializable {

	 /* (non-Javadoc)
 	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 	 */
 	public int compare(Object spatialObject1, Object spatialObject2) {
 		if(spatialObject1 instanceof Envelope)
 		{
 		    if(((Envelope) spatialObject1).getMinX()>((Envelope) spatialObject2).getMinX())
 		    {
 		    	return 1;
 		    }
 		    else if (((Envelope) spatialObject1).getMinX()<((Envelope) spatialObject2).getMinX())
 		    {
 		    	return -1;
 		    }
 		    else
 		    {
 		    	return 0;
 		    }
 		}
 		else if(spatialObject1 instanceof Circle)
 		{
 		    if(((Circle) spatialObject1).getMBR().getMinX()>((Circle) spatialObject2).getMBR().getMinX())
 		    {
 		    	return 1;
 		    }
 		    else if (((Circle) spatialObject1).getMBR().getMinX()<((Circle) spatialObject2).getMBR().getMinX())
 		    {
 		    	return -1;
 		    }
 		    else
 		    {
 		    	return 0;
 		    }
 		}
 		else
 		{
 		    if(((Geometry) spatialObject1).getEnvelopeInternal().getMinX()>((Geometry) spatialObject2).getEnvelopeInternal().getMinX())
 		    {
 		    	return 1;
 		    }
 		    else if (((Geometry) spatialObject1).getEnvelopeInternal().getMinX()<((Geometry) spatialObject2).getEnvelopeInternal().getMinX())
 		    {
 		    	return -1;
 		    }
 		    else
 		    {
 		    	return 0;
 		    }
 		}
 	}
}
