package it.agilelab.bigdata.gis.domain.graphhopper

case class EnrichEdge(idNode: Int, isInitialNode: Boolean, typeOfRoute: String, node: Option[TracePoint])
