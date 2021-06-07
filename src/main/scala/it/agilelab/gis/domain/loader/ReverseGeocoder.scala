package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.exceptions.ReverseGeocodingError
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.loader.ReverseGeocoder.Index
import it.agilelab.gis.domain.models.ReverseGeocodingResponse

trait ReverseGeocoder {

  def reverseGeocode(
      point: IdentifiableGPSPoint,
      indices: Set[Index] = ReverseGeocoder.indices
  ): Either[ReverseGeocodingError, ReverseGeocodingResponse]

}

object ReverseGeocoder {

  /** Index trait represents applicable indices to the reverse geocoder.
    */
  sealed trait Index

  /** Boundaries index
    */
  case object Boundaries extends Index

  /** Streets index
    */
  case object Streets extends Index

  /** House numbers index.
    */
  case object HouseNumbers extends Index

  /** All applicable indices.
    */
  val indices: Set[Index] = Set(Boundaries, Streets, HouseNumbers)
}
