package it.agilelab.gis.domain.models

import it.agilelab.gis.core.model.output.OutputModel

object ReverseGeocodingResponse {

  def apply(
      id: String,
      osmStreet: OSMStreetAndHouseNumber,
      osmBoundary: OSMBoundary,
      distanceAndNumber: (Double, Option[String])
  ): ReverseGeocodingResponse =
    ReverseGeocodingResponse(
      id,
      osmStreet.street,
      osmBoundary.city,
      osmBoundary.county,
      osmBoundary.countyCode,
      osmBoundary.region,
      osmBoundary.country,
      osmBoundary.countryCode,
      osmBoundary.postalCode,
      distanceAndNumber._2,
      osmStreet.speedLimit,
      None,
      osmStreet.streetType.map(_.value),
      Some(distanceAndNumber._1)
    )

  def apply(id: String, osmStreet: Option[OSMStreet], osmBoundary: Option[OSMBoundary]): ReverseGeocodingResponse =
    ReverseGeocodingResponse(
      id,
      osmStreet.flatMap(_.street),
      osmBoundary.flatMap(_.city),
      osmBoundary.flatMap(_.county),
      osmBoundary.flatMap(_.countyCode),
      osmBoundary.flatMap(_.region),
      osmBoundary.flatMap(_.country),
      osmBoundary.flatMap(_.countryCode),
      osmBoundary.flatMap(_.postalCode),
      osmStreet.flatMap(_.streetType.map(_.value)),
      osmStreet.flatMap(_.speedLimit)
    )

}

case class ReverseGeocodingResponse(
    id: String,
    street: Option[String],
    city: Option[String],
    county: Option[String],
    countyCode: Option[String],
    region: Option[String],
    country: Option[String],
    countryCode: Option[String],
    postalIndex: Option[String] = None,
    addressRange: Option[String] = None,
    speedLimit: Option[Int] = None,
    speedCategory: Option[String] = None,
    roadType: Option[String] = None,
    distance: Option[Double] = None
) extends OutputModel
