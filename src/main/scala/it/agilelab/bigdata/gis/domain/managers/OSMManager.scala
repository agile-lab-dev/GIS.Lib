package it.agilelab.bigdata.gis.domain.managers

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.core.utils.{Logger, ManagerUtils}
import it.agilelab.bigdata.gis.domain.configuration.OSMManagerConfiguration
import it.agilelab.bigdata.gis.domain.exceptions.ReverseGeocodingError
import it.agilelab.bigdata.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.bigdata.gis.domain.loader.ReverseGeocoder
import it.agilelab.bigdata.gis.domain.models.{KnnResult, OSMBoundary, OSMStreetAndHouseNumber, ReverseGeocodingResponse}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

case class OSMManager(conf: Config) extends ReverseGeocoder with Logger {

  val osmConfig: OSMManagerConfiguration = OSMManagerConfiguration(conf)
  val indexManager: IndexManager = IndexManager(osmConfig.indexConf)

  override def reverseGeocode(point: IdentifiableGPSPoint): Either[ReverseGeocodingError, ReverseGeocodingResponse] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(makeAddress(point, reverseGeocodeQueryingBoundaries(queryPoint), reverseGeocodeQueryingStreets(queryPoint), queryPoint)) match {
      case Success(addr) => Right(addr)
      case Failure(ex) =>
        logger.error("Failed to reverse geocode points", ex)
        Left(ReverseGeocodingError(ex))
    }
  }

  private def reverseGeocodeQueryingBoundaries(queryPoint: Point): Option[(OSMBoundary, KnnResult)] = {

    @tailrec
    def queryBoundaryIndices(boundaryIndices: List[GeometryList[OSMBoundary]],
                             query: Point): Option[(OSMBoundary, KnnResult)] = {
      boundaryIndices match {
        case Nil => None
        case index :: indicesTail => val topPlace = KNNQueryMem.spatialKnnQuery(
          index,
          query,
          k = 10
        )
          .headOption
          .filter { case (boundary, _) => boundary.contains(query) }
          // we cannot use .orElse here because it won't be seen as tailrec
          if (topPlace.nonEmpty) {
            topPlace
          } else {
            queryBoundaryIndices(indicesTail, query)
          }
      }
    }

    if (osmConfig.addressTolMeters > ManagerUtils.NUMBERS_MAX_DISTANCE)
      logger.info("address_tol_meter is greater than the distance used to build the spatial index!")

    queryBoundaryIndices(List(indexManager.indexSet.boundaries, indexManager.indexSet.regions), queryPoint)
  }

  private def reverseGeocodeQueryingStreets(queryPoint: Point): Option[OSMStreetAndHouseNumber] = {

    val roadsResult: Seq[OSMStreetAndHouseNumber] = KNNQueryMem.spatialKnnQueryWithMaxDistance(
      indexManager.indexSet.streets,
      queryPoint,
      k = 10,
      osmConfig.roadTolMeters
    )

    //If filterEmptyStreets is enabled then we filter out all the not defined and empty streets
    roadsResult.find(p => !osmConfig.filterEmptyStreets || p.street.exists(_.trim.nonEmpty))
  }

  private def makeAddress(point: IdentifiableGPSPoint,
                          place: Option[(OSMBoundary, KnnResult)],
                          street: Option[OSMStreetAndHouseNumber],
                          queryPoint: Point): ReverseGeocodingResponse = {

    (place, street) match {
      case (None, _) => ReverseGeocodingResponse(point.id, None, None)
      case (Some((boundary, _)), None) => ReverseGeocodingResponse(point.id, None, Some(boundary))
      case (Some((boundary, _)), Some(streetResult)) =>
        ReverseGeocodingResponse(
          point.id,
          streetResult,
          boundary,
          streetResult.getDistanceAndNumber(queryPoint, osmConfig.addressTolMeters))
    }
  }
}