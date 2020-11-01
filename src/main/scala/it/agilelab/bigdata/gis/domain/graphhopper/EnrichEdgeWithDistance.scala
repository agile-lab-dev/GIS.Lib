package it.agilelab.bigdata.gis.domain.graphhopper

case class EnrichEdgeWithDistance(idNode: Int,
                                  isInitialNode: Boolean,
                                  node: Option[Point],
                                  distance: Double)
