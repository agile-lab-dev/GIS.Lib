/**
 * FILE: KNNQuery.java
 * PATH: org.datasyslab.geospark.spatialOperator.KNNQuery.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.domain.spatialOperator

import java.io.Serializable

import com.graphhopper.util.Helper
import com.graphhopper.util.shapes.Circle
import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.operation.distance.DistanceOp
import it.agilelab.bigdata.gis.domain.knnJudgement._
import it.agilelab.bigdata.gis.domain.models.KnnResult
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import scala.collection.JavaConverters._

/**
 * The Class KNNQuery.
 */
object KNNQueryMem extends Serializable {

	private final val R: Int = 6371

	/**
	 * Spatial knn query.
	 *
	 * @param spatialList the spatial RDD
	 * @param queryCenter the query center
	 * @param k           the k
	 * @param useIndex    the use index
	 * @return the list
	 */
	def spatialKnnQuery[T <: Geometry](spatialList: GeometryList[T], queryCenter: Point, k: Int, useIndex: Boolean = true, distance: Option[Double] = None, distanceOp: String = "Default"): List[(T, KnnResult)] = {
		// For each partation, build a priority queue that holds the topk
		//@SuppressWarnings("serial")
		if (useIndex) {
			if (spatialList.index == null) {
				throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null")
			}

			val candidates: Array[AnyRef] = KnnJudgementUsingIndexS.invoke(spatialList.index, queryCenter, k)

			val rowResult: Array[(AnyRef, KnnResult)] =
				candidates.map(
					x => distanceOp match {
						case "Default" => (x, KnnResult(new DistanceOp(x.asInstanceOf[Geometry], queryCenter)).convert(anglesToMeters))
						case _ => throw new IllegalArgumentException(s"Allows values are ['Default'] found '${distanceOp}'")
					})

			val filterOutTooFarResults =
				distance match {
					case Some(dist) => rowResult.filter(x => x._2.distance < dist )
					case None => rowResult
				}

			val sortedNearOnes = filterOutTooFarResults.sorted(new TupleDistanceOrdering())
			val topK = sortedNearOnes.take(k)

			topK.map(r => (r._1.asInstanceOf[T], r._2)).toList
		}
		else {
			if (spatialList.rawSpatialCollection == null) {
				throw new IllegalArgumentException("You can't invoke raw search if you built index, remove buildIndex from your load procedure")
			} else {
				val tmp = GeometryKnnJudgementS.invoke(spatialList.rawSpatialCollection.toIterator, queryCenter, k)

				val result: Array[(Object, KnnResult)] = tmp
					.map(x => distanceOp match {
						case "Default" => (x, KnnResult(new DistanceOp(x.asInstanceOf[Geometry], queryCenter)).convert(anglesToMeters))
						case _ => throw new IllegalArgumentException(s"Allows values are ['Default'] found '$distanceOp'")
					})

				val filterResult =
					distance match {
						case Some(dist) => result.filter(x => x._2.distance < dist )
						case None => result
					}

				filterResult
					.sorted(new TupleDistanceOrdering())
					.take(k)
					.map(r => (r._1.asInstanceOf[T], r._2)).toList
			}
		}
	}

	/**
	 * Spatial knn query. Results having a distance greater than d are omitted.
	 *
	 * @param spatialList the spatial RDD
	 * @param queryCenter the query center
	 * @param k the k
	 * @param d the maximum distance in meters
	 * @param useIndex the use index
	 * @return the list
	 */
	def spatialKnnQueryWithMaxDistance[T <: Geometry](spatialList: GeometryList[T], queryCenter: Point, k: Int, d: Double, useIndex: Boolean = true): List[T] = {
		// For each partation, build a priority queue that holds the topk
		//@SuppressWarnings("serial")
		if(useIndex)
		{
			if(spatialList.index == null) {
				throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null")
			}
			if(spatialList.contains(queryCenter.getCoordinate)) {
				val tmp = KnnJudgementUsingIndexS.invoke(spatialList.index, queryCenter,k)
				val result = tmp
					.filter(r => isWithinDistance(r.asInstanceOf[Geometry], queryCenter, d))
					.sorted(new GeometryDistanceOrdering(queryCenter))
					.take(k)
				result.map(r => r.asInstanceOf[T]).toList
			} else {
				List.empty[T]
			}
		}
		else
		{
			if(spatialList.rawSpatialCollection == null) {
				throw new IllegalArgumentException("You can't invoke raw search if you built index, remove buildIndex from your load procedure")
			}else {
				if(spatialList.contains(queryCenter.getCoordinate)) {
					val tmp: Array[Object] = GeometryKnnJudgementS.invoke(spatialList.rawSpatialCollection.toIterator, queryCenter, k)
					val result: Array[Object] = tmp
						.filter(r => isWithinDistance(r.asInstanceOf[Geometry], queryCenter, d))
						.sorted(new GeometryDistanceOrdering(queryCenter))
						.take(k)
					result.map(r => r.asInstanceOf[T]).toList
				} else {
					List.empty[T]
				}
			}
		}
	}

	/**
	 * Spatial query returning the indexed geometries intersecting the circle having as center the queryCenter and
	 * and a radius of d meters. Results are order by distance.
	 *
	 * @param spatialList the spatial RDD
	 * @param queryCenter the query center
	 * @param d the maximum distance in meters
	 * @param useIndex the use index
	 * @return the list
	 */
	def spatialQueryWithMaxDistance[T <: Geometry]( spatialList: GeometryList[T], queryCenter: Point , d: Double, useIndex: Boolean = true): List[T] = {
		if(useIndex)
		{
			if(spatialList.index == null) {
				throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null")
			}
			if(spatialList.contains(queryCenter.getCoordinate)) {
				val circleOfRadiusD = new Circle(queryCenter.getY,queryCenter.getX,d)

				val circleEnvelope = new Envelope(circleOfRadiusD.getBounds.minLon, circleOfRadiusD.getBounds.maxLon,
					circleOfRadiusD.getBounds.minLat, circleOfRadiusD.getBounds.maxLat)

				val result: Array[Object] =
					JudgementUsingIndexS
						.invoke(spatialList.index, circleEnvelope)
						.map(r => (r, distanceInMeters(r.asInstanceOf[T],queryCenter)))
						.sortBy(_._2)
						.map(_._1)

				if(result.isEmpty)
					List.empty[T]
				else
					result.map(r => r.asInstanceOf[T]).toList

			} else
				List.empty[T]
		}
		else
		{
			throw new IllegalArgumentException("You can't invoke raw search in SpatialQueryWithMaxDistance")

		}
	}

	def spatialKnnQueryJava[T <: Geometry](spatialList: GeometryList[T], queryCenter: Point, k: Int, useIndex: Boolean = true): java.util.List[T] = {
		spatialKnnQuery(spatialList, queryCenter, k, useIndex).map(_._1).asJava
	}

	def anglesToMeters(a: Double): Double = (a/360) * (2*R*1000*Math.PI)

	/**
	 * Return true if point p distance from at least one point of the geometry is not greater than d
	 *
	 * @param geom a geometry
	 * @param p the point
	 * @param d the maximum distance in meters
	 * @return true if g has at least a coordinate pair which distance from p is not greater than d
	 */
	private def isWithinDistance(geom: Geometry, p: Point, d: Double): Boolean = {
		val coordinates = geom.getCoordinates
		val calc = Helper.DIST_EARTH
		coordinates
			.map( coords => calc.calcDist(coords.y,coords.x,p.getCoordinate.y,p.getCoordinate.x)<=d)
			.reduce(_ || _)
	}

	private def distanceInMeters[T <: Geometry](g: T, p2: Point): Double = {
		val coordsSeq = g.getCoordinates
		coordsSeq.map(p1 => Helper.DIST_EARTH.calcDist(p1.y,p1.x,p2.getY,p2.getX)).min
	}


}
