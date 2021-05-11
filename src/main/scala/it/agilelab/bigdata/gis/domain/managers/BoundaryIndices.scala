package it.agilelab.bigdata.gis.domain.managers

import it.agilelab.bigdata.gis.domain.models.OSMBoundary

/** [[BoundaryIndices]] holds indices related to boundaries.
  *
  * @param regionIndex region boundary index.
  * @param cityIndex   city boundary index.
  */
case class BoundaryIndices(indices: Seq[List[OSMBoundary]])
