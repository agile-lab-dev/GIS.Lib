package it.agilelab.gis.core.utils

import com.vividsolutions.jts.geom.Geometry

import java.io.Serializable

/** @author andreaL
  */
class XMinComparator extends Ordering[Geometry] with Serializable {

  override def compare(spatialObject1: Geometry, spatialObject2: Geometry): Int =
    if (
      spatialObject1.getEnvelopeInternal.getMinX >
        spatialObject2.getEnvelopeInternal.getMinX
    ) 1
    else if (
      spatialObject1.getEnvelopeInternal.getMinX <
        spatialObject2.getEnvelopeInternal.getMinX
    ) -1
    else 0

}
