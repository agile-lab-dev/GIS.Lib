package it.agilelab.bigdata.gis.core.utils

import java.io.Serializable
import java.util.Comparator

import com.vividsolutions.jts.geom.{Envelope, Geometry}
import it.agilelab.bigdata.gis.core.model.geometry.Circle

/**
  * @author andreaL
  */
class XMinComparator extends Ordering[Any] with Serializable {
  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  override def compare(spatialObject1: Any, spatialObject2: Any): Int =

    spatialObject1 match {

      case envelope: Envelope =>
        if (envelope.getMinX > spatialObject2.asInstanceOf[Envelope].getMinX) 1
        else if (envelope.getMinX < spatialObject2.asInstanceOf[Envelope].getMinX) -1
        else 0

      case circle: Circle =>
        if (circle.getMBR.getMinX > spatialObject2.asInstanceOf[Circle].getMBR.getMinX) 1
        else if (circle.getMBR.getMinX < spatialObject2.asInstanceOf[Circle].getMBR.getMinX) -1
        else 0

      case _ =>
        if (spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMinX >
            spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMinX) 1
        else if (spatialObject1.asInstanceOf[Geometry].getEnvelopeInternal.getMinX <
                 spatialObject2.asInstanceOf[Geometry].getEnvelopeInternal.getMinX) -1
        else 0
    }

}
