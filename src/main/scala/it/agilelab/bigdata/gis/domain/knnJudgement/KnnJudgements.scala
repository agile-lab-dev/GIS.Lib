package it.agilelab.bigdata.gis.domain.knnJudgement

import java.util.PriorityQueue

import com.vividsolutions.jts.geom.{Envelope, Geometry, Point}
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.strtree.{GeometryItemDistance, STRtree}
import it.agilelab.bigdata.gis.core.knnJudgement.GeometryDistanceComparator

object KnnJudgementUsingIndexS {

  val geomItemDist = new GeometryItemDistance

  def invoke(treeIndex: SpatialIndex, queryCenter: Point, k: Int): Array[AnyRef] = {
    treeIndex match {
      case tree: STRtree =>
        // TODO: test multithread
        val knn: Array[AnyRef] = tree.kNearestNeighbour(queryCenter.getEnvelopeInternal, queryCenter, geomItemDist, k)
        knn
      case _ => throw new Exception("[KnnJudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
    }
  }

}

object NNJudgementUsingIndexS {

  def invoke(treeIndex: SpatialIndex, queryCenter: Point): AnyRef = {
    treeIndex match {
      case tree: STRtree =>
        tree.nearestNeighbour(queryCenter.getEnvelopeInternal, queryCenter, new GeometryItemDistance)
      case _ =>
        throw new Exception("[NNJudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")

    }
  }
}

object JudgementUsingIndexS {

  def invoke(treeIndex: SpatialIndex, queryObjEnvelope: Envelope): Array[AnyRef] = {
    treeIndex match {
      case tree: STRtree =>
        tree.query(queryObjEnvelope).toArray
      case _ =>
        throw new Exception("[JudgementUsingIndex][Call]QuadTree index doesn't support KNN search.")
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
