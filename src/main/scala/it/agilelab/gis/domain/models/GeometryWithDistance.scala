package it.agilelab.gis.domain.models

import org.locationtech.jts.geom.Geometry

case class GeometryWithDistance[T <: Geometry](geometry: T, distance: Double)
