package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory, Point }
import it.agilelab.gis.core.utils.{ Logger, ManagerUtils }
import it.agilelab.gis.domain.configuration.OSMManagerConfiguration
import it.agilelab.gis.domain.exceptions.ReverseGeocodingError
import it.agilelab.gis.domain.graphhopper.IdentifiableGPSPoint
import it.agilelab.gis.domain.loader.ReverseGeocoder
import it.agilelab.gis.domain.loader.ReverseGeocoder.{ Boundaries, HouseNumbers, Index, Streets }
import it.agilelab.gis.domain.models._
import it.agilelab.gis.domain.spatialList.GeometryList
import it.agilelab.gis.domain.spatialOperator.KNNQueryMem

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }

case class OSMManager(conf: Config) extends ReverseGeocoder with Logger {

  val osmConfig: OSMManagerConfiguration = OSMManagerConfiguration(conf)
  val indexManager: IndexManager = IndexManager(osmConfig.indexConf)

  override def reverseGeocode(
      point: IdentifiableGPSPoint,
      indices: Set[Index] = ReverseGeocoder.indices
  ): Either[ReverseGeocodingError, ReverseGeocodingResponse] = {
    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    Try(
      makeAddress(
        point,
        reverseGeocodeQueryingBoundaries(queryPoint, indices),
        reverseGeocodeQueryingStreets(queryPoint, indices),
        queryPoint)) match {
      case Success(addr) => Right(addr)
      case Failure(ex) =>
        logger.error("Failed to reverse geocode points", ex)
        Left(ReverseGeocodingError(ex))
    }
  }

  private def reverseGeocodeQueryingBoundaries(
      queryPoint: Point,
      indices: Set[Index]
  ): Option[(OSMBoundary, KnnResult)] = {

    @tailrec
    def queryBoundaryIndices(
        boundaryIndices: List[GeometryList[OSMBoundary]],
        query: Point
    ): Option[(OSMBoundary, KnnResult)] =
      boundaryIndices match {
        case Nil => None
        case index :: indicesTail =>
          val topPlace = KNNQueryMem
            .spatialKnnQuery(
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

    if (osmConfig.addressTolMeters > ManagerUtils.NUMBERS_MAX_DISTANCE)
      logger.info("address_tol_meter is greater than the distance used to build the spatial index!")

    if (indices.contains(Boundaries))
      queryBoundaryIndices(List(indexManager.indexSet.boundaries), queryPoint)
    else
      None
  }

  private def reverseGeocodeQueryingStreets(queryPoint: Point, indices: Set[Index]): Option[OSMStreetAndHouseNumber] = {

    val roadsResult =
      if (indices.contains(Streets))
        KNNQueryMem.spatialKnnQueryWithMaxDistance(
          indexManager.indexSet.streets,
          queryPoint,
          k = 10,
          osmConfig.roadTolMeters
        )
      else
        Seq()

    val houseNumbers =
      if (indices.contains(HouseNumbers))
        KNNQueryMem.spatialQueryWithMaxDistance(
          indexManager.indexSet.houseNumbers,
          queryPoint,
          ManagerUtils.NUMBERS_MAX_DISTANCE
        )
      else
        Seq()

    //If filterEmptyStreets is enabled then we filter out all the not defined and empty streets
    roadsResult
      .find(p => !osmConfig.filterEmptyStreets || p.street.exists(_.trim.nonEmpty))
      .map(s =>
        s.copy(numbers = houseNumbers.map(n =>
          OSMSmallAddressNumber(
            n.point.getCoordinate.x,
            n.point.getCoordinate.y,
            n.number
          ))))
  }

  private def makeAddress(
      point: IdentifiableGPSPoint,
      place: Option[(OSMBoundary, KnnResult)],
      street: Option[OSMStreetAndHouseNumber],
      queryPoint: Point
  ): ReverseGeocodingResponse =
    ReverseGeocodingResponse(
      point,
      place,
      street,
      street.map(_.getDistanceAndNumber(queryPoint, osmConfig.addressTolMeters))
    )
}
