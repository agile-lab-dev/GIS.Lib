/**
 * FILE: KNNQuery.java
 * PATH: org.datasyslab.geospark.spatialOperator.KNNQuery.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.domain.spatialOperator

import com.graphhopper.util.Helper
import com.graphhopper.util.shapes.Circle
import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.operation.distance.DistanceOp
import it.agilelab.bigdata.gis.domain.knnJudgement._
import it.agilelab.bigdata.gis.domain.models.KnnResult
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import java.io.Serializable
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

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
  def spatialKnnQuery[T <: Geometry : ClassTag](spatialList: GeometryList[T],
                                                queryCenter: Point,
                                                k: Int,
                                                useIndex: Boolean = true,
                                                distance: Option[Double] = None,
                                                distanceOp: String = "Default"): List[(T, KnnResult)] = {
    // For each partition, build a priority queue that holds the top-K
    //@SuppressWarnings("serial")
    if (useIndex) {
      if (spatialList.index == null) {
        throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null")
      }
      val rowResult: Array[(T, KnnResult)] =
        KnnJudgementUsingIndexS
          .invoke[T](spatialList.index, queryCenter, k)
          .map(x => distanceOp match {
            case "Default" => (x, KnnResult(new DistanceOp(x, queryCenter)).convert(anglesToMeters))
            case _ => throw new IllegalArgumentException(s"Allows values are ['Default'] found '$distanceOp'")
          })

      val filterOutTooFarResults =
        distance match {
          case Some(dist) => rowResult.filter(x => x._2.distance < dist)
          case None => rowResult
        }

      filterOutTooFarResults.sorted(new TupleDistanceOrdering()).take(k).toList
    } else {
      if (spatialList.rawSpatialCollection == null) {
        throw new IllegalArgumentException("You can't invoke raw search if you built index, remove buildIndex from " +
          "your load procedure")
      } else {
        val result: Array[(T, KnnResult)] = GeometryKnnJudgementS
          .invoke[T](spatialList.rawSpatialCollection.toIterator.asInstanceOf[Iterator[T]], queryCenter, k)
          .map(x => distanceOp match {
            case "Default" => (x, KnnResult(new DistanceOp(x, queryCenter)).convert(anglesToMeters))
            case _ => throw new IllegalArgumentException(s"Allows values are ['Default'] found '$distanceOp'")
          })

        val filterResult: Array[(T, KnnResult)] =
          distance match {
            case Some(dist) => result.filter(x => x._2.distance < dist)
            case None => result
          }

        filterResult
          .sorted(new TupleDistanceOrdering())
          .take(k)
          .toList
      }
    }
  }

  /**
   * Spatial knn query. Results having a distance greater than d are omitted.
   *
   * @param spatialList the spatial RDD
   * @param queryCenter the query center
   * @param k           the k
   * @param distance    the maximum distance in meters
   * @param useIndex    the use index
   * @return the list
   */
  def spatialKnnQueryWithMaxDistance[T <: Geometry](spatialList: GeometryList[T],
                                                    queryCenter: Point,
                                                    k: Int,
                                                    distance: Double,
                                                    useIndex: Boolean = true): List[T] = {
    // For each partition, build a priority queue that holds the top-K
    //@SuppressWarnings("serial")
    if (useIndex) {
      if (spatialList.index == null) {
        throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null")
      }
      if (spatialList.contains(queryCenter.getCoordinate)) {
        KnnJudgementUsingIndexS
          .invoke[T](spatialList.index, queryCenter, k)
          .filter(isWithinDistance(_, queryCenter, distance))
          .sorted(new GeometryDistanceOrdering(queryCenter))
          .take(k)
          .toList
      } else {
        List.empty[T]
      }
    } else {
      if (spatialList.rawSpatialCollection == null) {
        throw new IllegalArgumentException("You can't invoke raw search if you built index, remove buildIndex from " +
          "your load procedure")
      } else {
        if (spatialList.contains(queryCenter.getCoordinate)) {
          GeometryKnnJudgementS
            .invoke(spatialList.rawSpatialCollection.toIterator.asInstanceOf[Iterator[Geometry]], queryCenter, k)
            .filter(isWithinDistance(_, queryCenter, distance))
            .sorted(new GeometryDistanceOrdering(queryCenter))
            .take(k)
            .map(_.asInstanceOf[T])
            .toList
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
   * @param distance    the maximum distance in meters
   * @param useIndex    the use index
   * @return the list
   */
  def spatialQueryWithMaxDistance[T <: Geometry](spatialList: GeometryList[T],
                                                 queryCenter: Point,
                                                 distance: Double,
                                                 useIndex: Boolean = true): List[T] = {
    if (useIndex) {
      if (spatialList.index == null) {
        throw new NullPointerException("Need to invoke buildIndex() first, indexedCollectionNoId is null")
      }
      if (spatialList.contains(queryCenter.getCoordinate)) {
        val circleOfRadiusD = new Circle(queryCenter.getY, queryCenter.getX, distance)
        val circleEnvelope = new Envelope(
          circleOfRadiusD.getBounds.minLon,
          circleOfRadiusD.getBounds.maxLon,
          circleOfRadiusD.getBounds.minLat,
          circleOfRadiusD.getBounds.maxLat
        )
        JudgementUsingIndexS
          .invoke[T](spatialList.index, circleEnvelope)
          .map(r => (r, distanceInMeters(r, queryCenter)))
          .sortBy(_._2)
          .map(_._1)
          .toList
      } else {
        List.empty[T]
      }
    } else {
      throw new IllegalArgumentException("You can't invoke raw search in SpatialQueryWithMaxDistance")
    }
  }

  def spatialKnnQueryJava[T <: Geometry : ClassTag](spatialList: GeometryList[T],
                                                    queryCenter: Point,
                                                    k: Int,
                                                    useIndex: Boolean = true): java.util.List[T] = {
    spatialKnnQuery(spatialList, queryCenter, k, useIndex).map(_._1).asJava
  }

  def anglesToMeters(a: Double): Double = (a / 360) * (2 * R * 1000 * Math.PI)

  /**
   * Return true if point p distance from at least one point of the geometry is not greater than d
   *
   * @param geometry a geometry
   * @param point    the point
   * @param distance the maximum distance in meters
   * @return true if g has at least a coordinate pair which distance from p is not greater than d
   */
  private def isWithinDistance(geometry: Geometry, point: Point, distance: Double): Boolean = {
    geometry
      .getCoordinates
      .map(coordinates => Helper.DIST_EARTH
        .calcDist(coordinates.y, coordinates.x, point.getCoordinate.y, point.getCoordinate.x) <= distance)
      .reduce(_ || _)
  }

  private def distanceInMeters[T <: Geometry](geometry: T, point: Point): Double = {
    geometry.getCoordinates.map(p => Helper.DIST_EARTH.calcDist(p.y, p.x, point.getY, point.getX)).min
  }
}
