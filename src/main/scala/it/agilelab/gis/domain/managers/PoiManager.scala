package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.configuration.PoiManagerConfiguration
import it.agilelab.gis.domain.exceptions.PoiSearchError
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.models._
import it.agilelab.gis.domain.spatialOperator.KNNQueryMem
import org.locationtech.jts.geom._

import scala.util.{ Failure, Success, Try }

/** [[PoiManager]] holds all methods to query for the different point of interest supported:
  * - amenity
  * - landuse
  * - leisure
  * - natural
  * - shop
  */
case class PoiManager(conf: Config) extends Logger {

  val config: PoiManagerConfiguration = PoiManagerConfiguration(conf)
  val indexManager: PoiIndexManager = PoiIndexManager(config.indexConf)

  def findAmenity(
      point: IdentifiableGPSPoint,
      distanceInMeters: Double
  ): Either[PoiSearchError, List[GeometryWithDistance[OSMPoiAmenity]]] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(
      KNNQueryMem
        .spatialQueryWithMaxDistanceIntegrated(
          indexManager.indexSet.amenity,
          queryPoint,
          distanceInMeters
        )
        .filter(i => !config.filterEmptyAmenity || i.geometry.amenity.exists(_.trim.nonEmpty))) match {
      case Success(res) => Right(res)
      case Failure(ex) =>
        logger.error("Failed to find amenity poi", ex)
        Left(PoiSearchError(ex))
    }
  }

  def findLanduse(
      point: IdentifiableGPSPoint,
      distanceInMeters: Double
  ): Either[PoiSearchError, List[GeometryWithDistance[OSMPoiLanduse]]] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(
      KNNQueryMem
        .spatialQueryWithMaxDistanceIntegrated(
          indexManager.indexSet.landuse,
          queryPoint,
          distanceInMeters
        )
        .filter(i => !config.filterEmptyLanduse || i.geometry.landuse.exists(_.trim.nonEmpty))) match {
      case Success(res) => Right(res)
      case Failure(ex) =>
        logger.error("Failed to find landuse poi", ex)
        Left(PoiSearchError(ex))
    }
  }

  def findLeisure(
      point: IdentifiableGPSPoint,
      distanceInMeters: Double
  ): Either[PoiSearchError, List[GeometryWithDistance[OSMPoiLeisure]]] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(
      KNNQueryMem
        .spatialQueryWithMaxDistanceIntegrated(
          indexManager.indexSet.leisure,
          queryPoint,
          distanceInMeters
        )
        .filter(i => !config.filterEmptyLeisure || i.geometry.leisure.exists(_.trim.nonEmpty))) match {
      case Success(res) => Right(res)
      case Failure(ex) =>
        logger.error("Failed to find leisure poi", ex)
        Left(PoiSearchError(ex))
    }
  }

  def findNatural(
      point: IdentifiableGPSPoint,
      distanceInMeters: Double
  ): Either[PoiSearchError, List[GeometryWithDistance[OSMPoiNatural]]] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(
      KNNQueryMem
        .spatialQueryWithMaxDistanceIntegrated(
          indexManager.indexSet.natural,
          queryPoint,
          distanceInMeters
        )
        .filter(i => !config.filterEmptyNatural || i.geometry.natural.exists(_.trim.nonEmpty))) match {
      case Success(res) => Right(res)
      case Failure(ex) =>
        logger.error("Failed to find natural poi", ex)
        Left(PoiSearchError(ex))
    }
  }

  def findShop(
      point: IdentifiableGPSPoint,
      distanceInMeters: Double
  ): Either[PoiSearchError, List[GeometryWithDistance[OSMPoiShop]]] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(
      KNNQueryMem
        .spatialQueryWithMaxDistanceIntegrated(
          indexManager.indexSet.shop,
          queryPoint,
          distanceInMeters
        )
        .filter(i => !config.filterEmptyShop || i.geometry.shop.exists(_.trim.nonEmpty))) match {
      case Success(res) => Right(res)
      case Failure(ex) =>
        logger.error("Failed to find shop poi", ex)
        Left(PoiSearchError(ex))
    }
  }

}
