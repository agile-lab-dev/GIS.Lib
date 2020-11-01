package it.agilelab.bigdata.gis.domain.graphhopper

case class EnrichEdge(idNode: Int, isInitialNode: Boolean, node: Option[Point])
