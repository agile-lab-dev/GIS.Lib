package it.agilelab.bigdata.gis.domain.graphhopper

case class EnrichEdgeWithDistance(idNode: Int,
                                  isInitialNode: Boolean,
                                  typeOfRoute: String,
                                  node: Option[TracePoint],
                                  distance: Double)
