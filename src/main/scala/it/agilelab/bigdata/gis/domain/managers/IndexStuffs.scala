package it.agilelab.bigdata.gis.domain.managers

import it.agilelab.bigdata.gis.domain.models.OSMBoundary

case class IndexStuffs(regionIndex: Seq[OSMBoundary], cityIndex: Seq[OSMBoundary])
