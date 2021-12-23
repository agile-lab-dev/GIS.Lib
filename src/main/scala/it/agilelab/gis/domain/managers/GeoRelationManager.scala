package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory }
import it.agilelab.gis.core.utils.ConfigurationProperties.INDEX
import it.agilelab.gis.core.utils.{ Configuration, Logger }
import it.agilelab.gis.domain.configuration.GeocodeManagerConfiguration
import it.agilelab.gis.domain.exceptions.{ InsideSeaError, NearestRailwayError }
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.loader.GeoRelation
import it.agilelab.gis.domain.models._
import it.agilelab.gis.domain.spatialOperator.KNNQueryMem
import it.agilelab.gis.utils.ScalaUtils.recordDuration

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.{ Failure, Success, Try }

case class GeoRelationManager(conf: Config) extends GeoRelation with Configuration with Logger {

  val indexManager: GeoRelationIndexManager = GeoRelationIndexManager(read[Config](conf, INDEX.value).get)

  override def nearestRailway(
      point: IdentifiableGPSPoint
  ): Either[NearestRailwayError, NearestRailwayResponse] = {
    val queryPoint = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))

    Try {
      val railwaysIndex = indexManager.indexSet.railways.get
      recordDuration(
        KNNQueryMem.spatialKnnQuery(railwaysIndex, queryPoint, 1).head,
        d => logger.info(s"Computed nearest railway from the point $point in $d ms")
      )
    } match {
      case Success(nearest) =>
        Right(
          NearestRailwayResponse(
            point.id,
            Option(nearest._2.distance),
            nearest._1.railway,
            nearest._1.railwayType.map(_.value),
            nearest._1.operator,
            nearest._1.usage.map(_.value)
          ))

      case Failure(ex) =>
        logger.error("Failed to compute the nearest railway", ex)
        Left(NearestRailwayError(ex))

    }
  }

  override def isInsideSea(point: IdentifiableGPSPoint): Either[InsideSeaError, InsideSeaResponse] = {
    val queryPoint = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))

    Try {
      val seaSpatialIndex = indexManager.indexSet.sea.get.index
      recordDuration(
        seaSpatialIndex
          .query(queryPoint.getEnvelopeInternal)
          .asScala
          .exists(_.asInstanceOf[OSMSea].polygon.contains(queryPoint)),
        d => logger.info(s"Checked if point $point is inside the sea in $d ms")
      )
    } match {
      case Success(isInsideSea) =>
        Right(InsideSeaResponse(point.id, Option(isInsideSea)))

      case Failure(ex) =>
        logger.error("Failed to check if the point is inside the sea", ex)
        Left(InsideSeaError(ex))
    }
  }
}
