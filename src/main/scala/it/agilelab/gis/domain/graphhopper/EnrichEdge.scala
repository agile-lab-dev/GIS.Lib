package it.agilelab.gis.domain.graphhopper

case class EnrichEdge(idNode: Int, isInitialNode: Boolean, typeOfRoute: String, node: Option[TracePoint])
