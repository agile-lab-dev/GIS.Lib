package it.agilelab.bigdata.gis.models

class GeometryMetadata(id: String)

class Street(id: String, streetName: String, city: String, country: String, speedLimit: Int, bidirected: Boolean) extends GeometryMetadata(id)

case class PartialAddress(street: String, city: String)