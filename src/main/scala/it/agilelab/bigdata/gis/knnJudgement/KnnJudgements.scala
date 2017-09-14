package it.agilelab.bigdata.gis.knnJudgement

import java.util.PriorityQueue

import com.vividsolutions.jts.geom.{Geometry, Point}
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.strtree.{GeometryItemDistance, STRtree}

/**
  * Created by paolo on 25/01/2017.
  */
object KnnJudgementUsingIndexS {

  def invoke(treeIndex: SpatialIndex, queryCenter: Point, k: Int) = {
    if (treeIndex.isInstanceOf[STRtree])
      treeIndex.asInstanceOf[STRtree].kNearestNeighbour(queryCenter.getEnvelopeInternal, queryCenter, new GeometryItemDistance, k)
    else throw new Exception("[KnnJudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
  }

}


object GeometryKnnJudgementS {

  def invoke(input: Iterator[Object], queryCenter: Point, k: Int) = {
    val pq: PriorityQueue[Geometry] = new PriorityQueue[Geometry](k, new GeometryDistanceComparator(queryCenter))
    while (input.hasNext) if (pq.size < k) pq.offer(input.next.asInstanceOf[Geometry])
    else {
      val curpoint: Geometry = input.next.asInstanceOf[Geometry]
      val distance: Double = curpoint.getCoordinate.distance(queryCenter.getCoordinate)
      val largestDistanceInPriQueue: Double = pq.peek.getCoordinate.distance(queryCenter.getCoordinate)
      if (largestDistanceInPriQueue > distance) {
        pq.poll
        pq.offer(curpoint)
      }
    }

    var res: Array[Object]= new Array[Object](k)
    var i: Int = 0
    while (i < k) {
      {
        res = res.:+(pq.poll)
      }
      {
        i += 1; i - 1
      }
    }
    res
  }

}
