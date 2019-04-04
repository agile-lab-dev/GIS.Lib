package it.agilelab.bigdata.gis.domain.models

class GeometryMetadata(id: String)

class Street(id: String, streetName: String, city: String, country: String, speedLimit: Int, bidirected: Boolean) extends GeometryMetadata(id)

object Address{
  def apply(osmStreet: Option[OSMStreet], osmBoundary: Option[OSMBoundary]): Address = {
    Address(
      osmStreet.flatMap(_.street),
      osmBoundary.flatMap(_.city),
      osmBoundary.flatMap(_.county),
      osmBoundary.flatMap(_.region),
      osmBoundary.flatMap(_.country))
  }
}

case class Address(street: Option[String], city: Option[String], county: Option[String], region: Option[String], country: Option[String])