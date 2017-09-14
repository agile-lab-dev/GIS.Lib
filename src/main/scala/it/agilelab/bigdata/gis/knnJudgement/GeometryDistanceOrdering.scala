package it.agilelab.bigdata.gis.knnJudgement

import com.vividsolutions.jts.geom.Point

/**
  * Created by paolo on 25/01/2017.
  */
class GeometryDistanceOrdering(queryCenter: Point) extends Ordering[Any] {
  override def compare(x: Any, y: Any): Int = new GeometryDistanceComparator(queryCenter).compare(x,y)
}
