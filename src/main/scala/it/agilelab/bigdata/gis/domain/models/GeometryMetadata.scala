package it.agilelab.bigdata.gis.domain.models

import it.agilelab.bigdata.gis.core.model.output.OutputModel

object Address {

  def apply(osmStreet: OSMStreetAndHouseNumber, osmBoundary: OSMBoundary, distanceAndNumber: (Double, Option[String])): Address = {
    Address(
      osmStreet.street,
      osmBoundary.city,
      osmBoundary.county,
      osmBoundary.region,
      osmBoundary.country,
      None,
      distanceAndNumber._2,
      osmStreet.speedLimit,
      None,
      osmStreet.streetType.map(_.value),
      Some(distanceAndNumber._1))
  }

  def apply(osmStreet: Option[OSMStreet], osmBoundary: Option[OSMBoundary]): Address = {
    Address(
      osmStreet.flatMap(_.street),
      osmBoundary.flatMap(_.city),
      osmBoundary.flatMap(_.county),
      osmBoundary.flatMap(_.region),
      osmBoundary.flatMap(_.country),
      None,
      osmStreet.flatMap(_.streetType.map(_.value)),
      osmStreet.flatMap(_.speedLimit))
  }

}

case class Address(street: Option[String],
                   city: Option[String],
                   county: Option[String],
                   region: Option[String],
                   country: Option[String],
                   postalIndex: Option[String] = None,
                   addressRange: Option[String] = None,
                   speedLimit: Option[Int] = None,
                   speedCategory: Option[String] = None,
                   roadType: Option[String] = None,
                   distance: Option[Double] = None) extends OutputModel