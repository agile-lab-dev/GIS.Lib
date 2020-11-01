package it.agilelab.bigdata.gis.domain.knnJudgement

import com.vividsolutions.jts.geom.Point
import it.agilelab.bigdata.gis.core.knnJudgement.GeometryDistanceComparator
import it.agilelab.bigdata.gis.domain.models.KnnResult

/**
  * Created by paolo on 25/01/2017.
  */
class GeometryDistanceOrdering(queryCenter: Point) extends Ordering[Any] {
  override def compare(x: Any, y: Any): Int = new GeometryDistanceComparator(queryCenter).compare(x,y)
}

/**
 * Simple tuple distance ordering. ATTENTION: ordering is descending
 */
class TupleDistanceOrdering() extends Ordering[(Any, KnnResult)] {
  override def compare(x: (Any,KnnResult), y: (Any,KnnResult)): Int = {
    val distance1 = x._2.distance
    val distance2 = y._2.distance
    if (distance1 > distance2) return 1
    else if (distance1 == distance2) return 0
    return -1
  }
}

