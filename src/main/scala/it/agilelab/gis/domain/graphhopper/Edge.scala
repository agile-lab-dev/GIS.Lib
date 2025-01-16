package it.agilelab.gis.domain.graphhopper

import com.graphhopper.matching.{EdgeMatch, State}

case class Edge(
    edge: EdgeMatch,
    item: State
)
