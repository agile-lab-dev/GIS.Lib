package it.agilelab.bigdata.gis.domain.loader

import it.agilelab.bigdata.gis.domain.graphhopper.GPSPoint
import it.agilelab.bigdata.gis.domain.models.{ReverseGeocodingError, ReverseGeocodingResponse}

trait ReverseGeocoder {

  def reverseGeocode(point: GPSPoint) : Either[ReverseGeocodingError, ReverseGeocodingResponse]

}
