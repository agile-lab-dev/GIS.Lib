package it.agilelab.gis.domain.graphhopper

import com.graphhopper.matching.{ EdgeMatch, GPXExtension }

case class Edge(
    edge: EdgeMatch,
    item: GPXExtension
)
