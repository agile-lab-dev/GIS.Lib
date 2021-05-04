package it.agilelab.bigdata.gis.core.utils

import java.io.Serializable
import java.util.Comparator

import com.vividsolutions.jts.geom.{ Envelope, Geometry }
import it.agilelab.bigdata.gis.core.model.geometry.Circle

/** @author andreaL
  */
class YMinComparator extends Ordering[Any] with Serializable {

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  override def compare(spatialObject1: Any, spatialObject2: Any): Int =
    spatialObject1 match {

      case envelope: Envelope =>
        if (envelope.getMinY > spatialObject2.asInstanceOf[Envelope].getMinY) 1
        else if (envelope.getMinY < spatialObject2.asInstanceOf[Envelope].getMinY) -1
        else 0

      case circle: Circle =>
        if (circle.getMBR.getMinY > spatialObject2.asInstanceOf[Circle].getMBR.getMinY) 1
        else if (circle.getMBR.getMinY < spatialObject2.asInstanceOf[Circle].getMBR.getMinY) -1
        else 0

      case _ =>
        if (
          spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMinY >
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMinY
        ) 1
        else if (
          spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMinY <
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMinY
        ) -1
        else 0
    }
}
