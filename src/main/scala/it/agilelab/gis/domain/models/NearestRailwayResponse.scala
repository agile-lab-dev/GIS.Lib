package it.agilelab.gis.domain.models

import it.agilelab.gis.core.model.output.OutputModel

case class NearestRailwayResponse(
    id: String,
    distance: Option[Double],
    railway: Option[String] = None,
    railwayType: Option[String] = None,
    operator: Option[String] = None,
    usage: Option[String] = None
) extends OutputModel
    with Identifiable
