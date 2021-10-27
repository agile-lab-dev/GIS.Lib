package it.agilelab.gis.core.knnJudgement

import java.io.Serializable
import java.util.Comparator

import org.locationtech.jts.geom.{ Envelope, Point }

/** @author andreaL
  */
class RectangleDistanceComparator(var queryCenter: Point) extends Comparator[Envelope] with Serializable {

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  override def compare(p1: Envelope, p2: Envelope): Int = {

    val distance1 = p1.distance(queryCenter.getEnvelopeInternal)
    val distance2 = p2.distance(queryCenter.getEnvelopeInternal)

    if (distance1 > distance2) 1
    else if (distance1 == distance2) 0
    else -1
  }
}
