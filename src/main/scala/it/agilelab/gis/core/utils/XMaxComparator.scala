package it.agilelab.gis.core.utils

import org.locationtech.jts.geom.Geometry

import java.io.Serializable

/** @author andreaL
  */
class XMaxComparator extends Ordering[Geometry] with Serializable {

  override def compare(spatialObject1: Geometry, spatialObject2: Geometry): Int =
    if (
      spatialObject1.getEnvelopeInternal.getMaxX >
        spatialObject2.getEnvelopeInternal.getMaxX
    ) 1
    else if (
      spatialObject1.getEnvelopeInternal.getMaxX <
        spatialObject2.getEnvelopeInternal.getMaxX
    ) -1
    else 0

}
