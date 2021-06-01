package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.exceptions.ReverseGeocodingError
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.loader.Index.Index
import it.agilelab.gis.domain.models.ReverseGeocodingResponse

trait ReverseGeocoder {

  def reverseGeocode(
      point: IdentifiableGPSPoint,
      indices: Set[Index] = Index.values
  ): Either[ReverseGeocodingError, ReverseGeocodingResponse]

}

/** Enumeration of supported indices.
  */
object Index extends Enumeration {
  type Index = Value

  val Boundaries, Street, HouseNumber = Value
}
