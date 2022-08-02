package it.agilelab.gis.core.knnJudgement

import org.locationtech.jts.geom.{ Geometry, Point }

import java.io.Serializable
import java.util.Comparator

/** @author andreaL
  */
class GeometryDistanceComparator(var queryCenter: Point) extends Comparator[Any] with Serializable {

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */

  override def compare(p1: Any, p2: Any): Int = {

    val distance1 = p1.asInstanceOf[Geometry].distance(queryCenter)
    val distance2 = p2.asInstanceOf[Geometry].distance(queryCenter)

    if (distance1 > distance2) 1
    else if (distance1 == distance2) 0
    else -1

  }

}
