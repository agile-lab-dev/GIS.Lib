package it.agilelab.bigdata.gis.domain.knnJudgement

import java.util.PriorityQueue

import com.vividsolutions.jts.geom.{Geometry, Point}
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.strtree.{GeometryItemDistance, STRtree}
import it.agilelab.bigdata.gis.core.knnJudgement.GeometryDistanceComparator

/**
  * Created by paolo on 25/01/2017.
  */
object KnnJudgementUsingIndexS {

  def invoke(treeIndex: SpatialIndex, queryCenter: Point, k: Int) = {
    treeIndex match {
      case rtree: STRtree =>
        rtree.kNearestNeighbour(queryCenter.getEnvelopeInternal, queryCenter, new GeometryItemDistance, k)
      case _ =>
        throw new Exception("[KnnJudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
    }
  }

}


object GeometryKnnJudgementS {

  def invoke(input: Iterator[Object], queryCenter: Point, k: Int): Array[Object] = {

    val pq: PriorityQueue[Geometry] =
      new PriorityQueue[Geometry](k, new GeometryDistanceComparator(queryCenter))

    while (input.hasNext) {

      if (pq.size < k) pq.offer(input.next.asInstanceOf[Geometry])
      else {

        val curpoint: Geometry = input.next.asInstanceOf[Geometry]
        val distance: Double = curpoint.getCoordinate.distance(queryCenter.getCoordinate)
        val largestDistanceInPriQueue: Double =
          pq.peek.getCoordinate.distance(queryCenter.getCoordinate)

        if (largestDistanceInPriQueue > distance) {
          pq.poll
          pq.offer(curpoint)
        }

      }
    }

    (0 until k).foldLeft(Array.empty[Object])((acc, z) => acc :+ pq.poll)

  }

}
