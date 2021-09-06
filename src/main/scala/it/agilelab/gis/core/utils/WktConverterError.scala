package it.agilelab.gis.core.utils

/** Trait implementing WKT Conversion errors
  */
sealed trait WKT_CONVERTER_ERROR
case class INVALID_WKT(ex: Throwable) extends WKT_CONVERTER_ERROR
