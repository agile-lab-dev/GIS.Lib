package it.agilelab.gis.domain.models

import it.agilelab.gis.core.model.output.OutputModel

case class InsideSeaResponse(
    id: String,
    isInside: Option[Boolean]
) extends OutputModel
    with Identifiable
