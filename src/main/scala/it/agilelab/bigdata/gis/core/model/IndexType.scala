package it.agilelab.bigdata.gis.core.model

/**
  * @author andreaL
  */

sealed trait IndexType

object IndexType {
  case object QUADTREE extends IndexType
  case object RTREE extends IndexType
}