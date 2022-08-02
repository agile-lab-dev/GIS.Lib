package it.agilelab.gis.domain.models

import it.agilelab.gis.core.model.output.OutputModel
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import org.locationtech.jts.geom.OSMStreetAndHouseNumber

object ReverseGeocodingResponse {

  def apply(
      point: IdentifiableGPSPoint,
      place: Option[(OSMBoundary, KnnResult)],
      street: Option[OSMStreetAndHouseNumber],
      distanceAndNumber: Option[(Double, Option[String])]
  ): ReverseGeocodingResponse = {

    val (boundary, _) = place match {
      case Some((b, r)) => (Some(b), Some(r))
      case None         => (None, None)
    }

    ReverseGeocodingResponse(
      id = point.id,
      street = street.flatMap(_.street),
      city = boundary.flatMap(_.city),
      county = boundary.flatMap(_.county),
      countyCode = boundary.flatMap(_.countyCode),
      region = boundary.flatMap(_.region),
      country = boundary.flatMap(_.country),
      countryCode = boundary.flatMap(_.countryCode),
      postalIndex = boundary.flatMap(_.postalCode),
      addressRange = distanceAndNumber.flatMap(_._2),
      speedLimit = street.flatMap(_.speedLimit),
      speedCategory = None,
      roadType = street.flatMap(_.streetType.map(_.value)),
      distance = distanceAndNumber.map(_._1)
    )
  }

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
    with Identifiable
