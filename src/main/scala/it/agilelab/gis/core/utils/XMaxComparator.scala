package it.agilelab.gis.core.utils

import java.io.Serializable
import java.util.Comparator

import com.vividsolutions.jts.geom.{ Envelope, Geometry }
import it.agilelab.gis.core.model.geometry.Circle

/** @author andreaL
  */
class XMaxComparator extends Ordering[Any] with Serializable {

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  override def compare(spatialObject1: Any, spatialObject2: Any): Int =
    spatialObject1 match {

      case envelope: Envelope =>
        if (envelope.getMaxX > spatialObject2.asInstanceOf[Envelope].getMaxX) 1
        else if (envelope.getMaxX < spatialObject2.asInstanceOf[Envelope].getMaxX) -1
        else 0

      case circle: Circle =>
        if (circle.getMBR.getMaxX > spatialObject2.asInstanceOf[Circle].getMBR.getMaxX) 1
        else if (circle.getMBR.getMaxX < spatialObject2.asInstanceOf[Circle].getMBR.getMaxX) -1
        else 0

      case _ =>
        if (
          spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMaxX >
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMaxX
        ) 1
        else if (
          spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMaxX <
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMaxX
        ) -1
        else 0
    }

}
