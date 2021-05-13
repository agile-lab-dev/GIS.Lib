package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.operation.distance.DistanceOp

/** All geometrical information should be contained here
  *
  * @param matched  closest point in the matched geometry
  * @param distance distance between the points
  */
case class KnnResult(matched: Coordinate, distance: Double) {

  def convert(converter: Double => Double): KnnResult =
    new KnnResult(matched, converter(distance))
}

object KnnResult {
  def apply(distanceOp: DistanceOp): KnnResult =
    KnnResult(distanceOp.nearestPoints().head, distanceOp.distance())

}
