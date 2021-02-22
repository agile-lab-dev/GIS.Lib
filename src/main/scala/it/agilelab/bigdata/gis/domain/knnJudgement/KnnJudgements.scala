package it.agilelab.bigdata.gis.domain.knnJudgement

import com.vividsolutions.jts.geom.{Envelope, Geometry, Point}
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.strtree.{GeometryItemDistance, STRtree}
import it.agilelab.bigdata.gis.core.knnJudgement.GeometryDistanceComparator

import java.util.PriorityQueue
import scala.reflect.ClassTag

object KnnJudgementUsingIndexS {

  val geomItemDist = new GeometryItemDistance

  def invoke[T](treeIndex: SpatialIndex,
                queryCenter: Point,
                k: Int): Array[T] = {
    treeIndex match {
      case tree: STRtree =>
        // TODO: test multithread
        tree.kNearestNeighbour(queryCenter.getEnvelopeInternal, queryCenter, geomItemDist, k).asInstanceOf[Array[T]]
      case _ => throw new Exception("[KnnJudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
    }
  }

}

object NNJudgementUsingIndexS {

  def invoke[T](treeIndex: SpatialIndex,
                queryCenter: Point): T = {
    treeIndex match {
      case tree: STRtree =>
        tree.nearestNeighbour(queryCenter.getEnvelopeInternal, queryCenter, new GeometryItemDistance).asInstanceOf[T]
      case _ => throw new Exception("[NNJudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
    }
  }
}

object JudgementUsingIndexS {

  def invoke[T](treeIndex: SpatialIndex,
                queryObjEnvelope: Envelope): Array[T] = {
    treeIndex match {
      case tree: STRtree => tree.query(queryObjEnvelope).toArray.asInstanceOf[Array[T]]
      case _ =>
        throw new Exception("[JudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
    }
  }
}

object GeometryKnnJudgementS {

  def invoke[T <: Geometry : ClassTag](input: Iterator[T],
                                       queryCenter: Point,
                                       k: Int): Array[T] = {

    val pq: PriorityQueue[T] = new PriorityQueue[T](k, new GeometryDistanceComparator(queryCenter))

    while (input.hasNext) {
      if (pq.size < k) {
        pq.offer(input.next)
      } else {
        val curPoint: T = input.next
        val distance: Double = curPoint.getCoordinate.distance(queryCenter.getCoordinate)
        val greatestDistanceInPriQueue: Double = pq.peek.getCoordinate.distance(queryCenter.getCoordinate)

        if (greatestDistanceInPriQueue > distance) {
          pq.poll
          pq.offer(curPoint)
        }
      }
    }
    pq.toArray[T](Array.empty[T])
  }
}
