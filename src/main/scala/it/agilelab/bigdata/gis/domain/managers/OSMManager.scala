package it.agilelab.bigdata.gis.domain.managers

import com.typesafe.config.Config
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.core.utils.{Logger, ManagerUtils}
import it.agilelab.bigdata.gis.domain.configuration.OSMManagerConfiguration
import it.agilelab.bigdata.gis.domain.graphhopper.GPSPoint
import it.agilelab.bigdata.gis.domain.loader.ReverseGeocoder
import it.agilelab.bigdata.gis.domain.models.{KnnResult, OSMBoundary, OSMStreetAndHouseNumber, ReverseGeocodingResponse}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem

import scala.annotation.tailrec
import scala.collection.immutable

case class OSMManager(conf: Config) extends ReverseGeocoder with Logger {

  val osmConfig: OSMManagerConfiguration = OSMManagerConfiguration(conf)
  val indexManager: IndexManager = IndexManager(osmConfig.indexConf)

  override def reverseGeocode(point: GPSPoint) : ReverseGeocodingResponse = {

    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(point.lon, point.lat))
    val administrativeBoundaries: Option[(OSMBoundary, KnnResult)] = reverseGeocodeQueryingBoundaries(queryPoint)
    val street: Option[OSMStreetAndHouseNumber] = reverseGeocodeQueryingStreets(queryPoint)
    makeAddress(administrativeBoundaries, street, queryPoint)
  }

  private def reverseGeocodeQueryingBoundaries(queryPoint: Point): Option[(OSMBoundary, KnnResult)] = {

    @tailrec
    def queryBoundaryIndices(boundaryIndices: List[GeometryList[OSMBoundary]],
                             query: Point): Option[(OSMBoundary, KnnResult)] = {
      boundaryIndices match {

        case Nil =>
          None

        case index :: indicesTail =>
          val places: immutable.Seq[(OSMBoundary, KnnResult)] =
            KNNQueryMem.spatialKnnQuery(
              index,
              query,
              10,
              distanceOp = "Default"
            )

          val topPlace: Option[(OSMBoundary, KnnResult)] = places.headOption

          val placeCoversQueriedPoint: Boolean =
            topPlace match {
              case Some(head) => head._1.contains(query)
              case None => false
            }

          if (placeCoversQueriedPoint) {
            topPlace
          } else {
            queryBoundaryIndices(indicesTail, query)
          }
      }
    }

    if(osmConfig.addressTolMeters > ManagerUtils.NUMBERS_MAX_DISTANCE)
      logger.info("address_tol_meter is greater than the distance used to build the spatial index!")

    queryBoundaryIndices(List(indexManager.indexSet.boundaries, indexManager.indexSet.regions), queryPoint)
  }

  private def reverseGeocodeQueryingStreets(queryPoint: Point): Option[OSMStreetAndHouseNumber] = {

    val roadsResult =
        KNNQueryMem
          .spatialKnnQueryWithMaxDistance(
            indexManager.indexSet.streets,
            queryPoint,
            10,
            osmConfig.roadTolMeters
          )

      //If filterEmptyStreets is enabled then we filter out all the not defined and empty streets
      val road: Option[OSMStreetAndHouseNumber] = roadsResult.find(p => !osmConfig.filterEmptyStreets || (p.street.isDefined && !p.street.get.trim.equals("")))
      road

  }

  private def makeAddress(place: Option[(OSMBoundary, KnnResult)],
                          street: Option[OSMStreetAndHouseNumber],
                          queryPoint: Point): ReverseGeocodingResponse = {

    (place, street) match {

      case (None, _) =>
        ReverseGeocodingResponse(None, None)

      case (Some(topBoundaryWithKnnResult), None) =>

        val topPlace: Option[OSMBoundary] =
          Some(topBoundaryWithKnnResult._1)
            .asInstanceOf[Option[OSMBoundary]]

        ReverseGeocodingResponse(None, topPlace)

      case (Some(topBoundaryWithKnnResult), Some(streetResult)) =>
        ReverseGeocodingResponse(
          streetResult,
          topBoundaryWithKnnResult._1,
          streetResult.getDistanceAndNumber(queryPoint, osmConfig.addressTolMeters))
    }
  }
}