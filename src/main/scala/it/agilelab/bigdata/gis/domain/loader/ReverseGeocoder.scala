package it.agilelab.bigdata.gis.domain.loader

import it.agilelab.bigdata.gis.domain.exceptions.ReverseGeocodingError
import it.agilelab.bigdata.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.bigdata.gis.domain.models.ReverseGeocodingResponse

trait ReverseGeocoder {

  def reverseGeocode(point: IdentifiableGPSPoint) : Either[ReverseGeocodingError, ReverseGeocodingResponse]

}
