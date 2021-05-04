package it.agilelab.bigdata.gis.core.model

/** @author andreaL
  */

sealed trait GridType

object GridType {
  case object EQUALGRID extends GridType
  case object HILBERT extends GridType
  case object RTREE extends GridType
  case object VORONOI extends GridType
}
