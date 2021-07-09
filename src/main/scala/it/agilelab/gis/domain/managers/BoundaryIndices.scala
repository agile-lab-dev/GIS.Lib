package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models.OSMBoundary

/** [[BoundaryIndices]] holds indices related to boundaries.
  *
  * @param index boundary index
  */
case class BoundaryIndices(index: Seq[OSMBoundary])
