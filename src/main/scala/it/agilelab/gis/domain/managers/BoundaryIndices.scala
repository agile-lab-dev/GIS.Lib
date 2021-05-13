package it.agilelab.gis.domain.managers

import it.agilelab.gis.domain.models.OSMBoundary

/** [[BoundaryIndices]] holds indices related to boundaries.
  *
  * @param regionIndex region boundary index.
  * @param cityIndex   city boundary index.
  */
case class BoundaryIndices(regionIndex: Seq[OSMBoundary], cityIndex: Seq[OSMBoundary])
