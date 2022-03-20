package it.agilelab.gis.core.utils

import java.io.Serializable

import it.agilelab.gis.core.model.geometry.Circle
import org.locationtech.jts.geom.{ Envelope, Geometry }

/** @author andreaL
  */
class YMaxComparator extends Ordering[Any] with Serializable {
  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  override def compare(spatialObject1: Any, spatialObject2: Any): Int =
    spatialObject1 match {

      case envelope: Envelope =>
        if (envelope.getMaxY > spatialObject2.asInstanceOf[Envelope].getMaxY) 1
        else if (envelope.getMaxY < spatialObject2.asInstanceOf[Envelope].getMaxY) -1
        else 0

      case circle: Circle =>
        if (circle.getMBR.getMaxY > spatialObject2.asInstanceOf[Circle].getMBR.getMaxY) 1
        else if (circle.getMBR.getMaxY < spatialObject2.asInstanceOf[Circle].getMBR.getMaxY) -1
        else 0

      case _ =>
        if (
          spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMaxY >
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMaxY
        ) 1
        else if (
          spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMaxY <
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMaxY
        ) -1
        else 0
    }
}
