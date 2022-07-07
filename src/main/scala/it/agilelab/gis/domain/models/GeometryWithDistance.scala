package it.agilelab.gis.domain.models

import com.vividsolutions.jts.geom.Geometry

case class GeometryWithDistance[T <: Geometry](geometry: T, distance: Double)
