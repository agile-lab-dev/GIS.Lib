package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.exceptions.ReverseGeocodingError
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.models.ReverseGeocodingResponse

trait ReverseGeocoder {

  def reverseGeocode(point: IdentifiableGPSPoint): Either[ReverseGeocodingError, ReverseGeocodingResponse]

}
