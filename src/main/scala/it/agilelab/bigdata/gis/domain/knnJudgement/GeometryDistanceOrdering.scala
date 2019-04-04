package it.agilelab.bigdata.gis.domain.knnJudgement

import com.vividsolutions.jts.geom.Point
import it.agilelab.bigdata.gis.core.knnJudgement.GeometryDistanceComparator

/**
  * Created by paolo on 25/01/2017.
  */
class GeometryDistanceOrdering(queryCenter: Point) extends Ordering[Any] {
  override def compare(x: Any, y: Any): Int = new GeometryDistanceComparator(queryCenter).compare(x,y)
}
