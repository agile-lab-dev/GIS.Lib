package it.agilelab.gis.core.utils

sealed trait GeometryTypeOperations {
  def name: String
}

object GeometryTypeOperations {

  case object Contains extends GeometryTypeOperations {
    override val name: String = "contains"
  }

  case object Distance extends GeometryTypeOperations {
    override val name: String = "distance"
  }

  case object Intersection extends GeometryTypeOperations {
    override val name: String = "intersection"
  }
}