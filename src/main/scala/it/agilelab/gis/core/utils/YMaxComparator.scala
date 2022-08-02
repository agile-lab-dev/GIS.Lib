package it.agilelab.gis.core.utils

import org.locationtech.jts.geom.Geometry

import java.io.Serializable

/** @author andreaL
  */
class YMaxComparator extends Ordering[Geometry] with Serializable {

  override def compare(spatialObject1: Geometry, spatialObject2: Geometry): Int =
    if (
      spatialObject1.getEnvelopeInternal.getMaxY >
        spatialObject2.getEnvelopeInternal.getMaxY
    ) 1
    else if (
      spatialObject1.getEnvelopeInternal.getMaxY <
        spatialObject2.getEnvelopeInternal.getMaxY
    ) -1
    else 0

}
