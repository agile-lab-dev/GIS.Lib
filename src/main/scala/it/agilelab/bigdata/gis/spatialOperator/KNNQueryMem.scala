/**
 * FILE: KNNQuery.java
 * PATH: org.datasyslab.geospark.spatialOperator.KNNQuery.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.spatialOperator

import com.vividsolutions.jts.geom._
import java.io.Serializable
import java.util.ArrayList

import it.agilelab.bigdata.gis.knnJudgement.{GeometryDistanceOrdering, GeometryKnnJudgementS, KnnJudgementUsingIndexS}
import it.agilelab.bigdata.gis.spatialList.GeometryList

import scala.collection.JavaConverters._



// TODO: Auto-generated Javadoc

/**
 * The Class KNNQuery.
 */
object KNNQueryMem extends Serializable {


	/**
	 * Spatial knn query.
	 *
	 * @param spatialList the spatial RDD
	 * @param queryCenter the query center
	 * @param k the k
	 * @param useIndex the use index
	 * @return the list
	 */
	def SpatialKnnQuery[T <: Geometry]( spatialList: GeometryList[T], queryCenter: Point , k: Int, useIndex: Boolean = true): List[T] = {
		// For each partation, build a priority queue that holds the topk
		//@SuppressWarnings("serial")
		if(useIndex)
		{
	        if(spatialList.index == null) {
	            throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null");
	        }
					val fact = new GeometryFactory()
					val tmp = KnnJudgementUsingIndexS.invoke(spatialList.index, queryCenter,k)
					val result = tmp.sorted(new GeometryDistanceOrdering(queryCenter)).take(k)
					result.map(r => r.asInstanceOf[T]).toList
		}
		else
		{
			if(spatialList.rawSpatialCollection == null) {
				throw new IllegalArgumentException("You can't invoke raw search if you built index, remove buildIndex from your load procedure")
			}else {
				val tmp = GeometryKnnJudgementS.invoke(spatialList.rawSpatialCollection.toIterator, queryCenter, k);
				val result: Array[Object] = tmp.sorted(new GeometryDistanceOrdering(queryCenter)).take(k)
				result.map(r => r.asInstanceOf[T]).toList
			}
		}
	}

	def SpatialKnnQueryJava[T <: Geometry]( spatialList: GeometryList[T], queryCenter: Point , k: Int, useIndex: Boolean = true): java.util.List[T] = {
		SpatialKnnQuery(spatialList, queryCenter, k, useIndex).asJava
	}

}
