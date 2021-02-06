package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import it.agilelab.bigdata.gis.core.utils.{Logger, ObjectPickler}
import it.agilelab.bigdata.gis.domain.managers.ManagerUtils.Path
import it.agilelab.bigdata.gis.domain.managers.{IndexManager, ManagerUtils}
import it.agilelab.bigdata.gis.domain.models.{ReverseGeocodingResponse, KnnResult, OSMBoundary, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList
import it.agilelab.bigdata.gis.domain.spatialOperator.KNNQueryMem

import scala.annotation.tailrec
import scala.collection.immutable

class OSMManager extends Logger {

  var boundariesGeometryList: GeometryList[OSMBoundary] = _
  var regionGeometryList: GeometryList[OSMBoundary] = _
  var roadsGeometryList: GeometryList[OSMStreetAndHouseNumber] = _

  /**
    * Initializes all geo lists
    * @throws IllegalArgumentException if input params are wrong
    */
  @throws[IllegalArgumentException]
  def init(paths: List[Path]): Unit = {

    val indices =
      if (paths.size == 1) {
        val index = IndexManager.makeIndices(paths.head)
        (index.boundaries, index.regions, index.streets)
      } else {
        val boundaries = ObjectPickler.unpickle[GeometryList[OSMBoundary]](paths.head)
        val regions = ObjectPickler.unpickle[GeometryList[OSMBoundary]](paths(1))
        val streets = ObjectPickler.unpickle[GeometryList[OSMStreetAndHouseNumber]](paths(2))
        (boundaries, regions, streets)
      }

    boundariesGeometryList = indices._1
    regionGeometryList = indices._2
    roadsGeometryList = indices._3
  }


  def reverseGeocode(latitude: Double, longitude: Double, filterEmptyStreets: Boolean = false,
                     road_tol_meters: Double = 100.0,  address_tol_meters: Double = 20.0) : ReverseGeocodingResponse = {

    val queryPoint: Point = new GeometryFactory().createPoint(new Coordinate(longitude, latitude))

    val administrativeBoundaries: Option[(OSMBoundary, KnnResult)] =
      reverseGeocodeQueryingBoundaries(queryPoint, filterEmptyStreets, road_tol_meters, address_tol_meters)

    val street: Option[OSMStreetAndHouseNumber] =
      reverseGeocodeQueryingStreets(queryPoint, filterEmptyStreets, road_tol_meters, address_tol_meters)

    makeAddress(administrativeBoundaries, street, queryPoint, address_tol_meters)
  }

  private def reverseGeocodeQueryingBoundaries(queryPoint: Point, filterEmptyStreets: Boolean = false,
                                               road_tol_meters: Double = 100.0, address_tol_meters: Double = 20.0): Option[(OSMBoundary, KnnResult)] = {

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

    if(address_tol_meters > ManagerUtils.NUMBERS_MAX_DISTANCE)
      logger.info("address_tol_meter is greater than the distance used to build the spatial index!")

    queryBoundaryIndices(
      List(boundariesGeometryList, regionGeometryList),
      queryPoint
    )
  }

  private def reverseGeocodeQueryingStreets(queryPoint: Point, filterEmptyStreets: Boolean = false,
                                            road_tol_meters: Double = 100.0, address_tol_meters: Double = 20.0): Option[OSMStreetAndHouseNumber] = {

    if (roadsGeometryList != null) {
      val roadsResult =
        KNNQueryMem
          .spatialKnnQueryWithMaxDistance(
            roadsGeometryList,
            queryPoint,
            10,
            road_tol_meters
          )

      //If filterEmptyStreets is enabled then we filter out all the not defined and empty streets
      val road: Option[OSMStreetAndHouseNumber] = roadsResult.find(p => !filterEmptyStreets || (p.street.isDefined && !p.street.get.trim.equals("")))

      road
    } else {
      None
    }

  }

  private def makeAddress(place: Option[(OSMBoundary, KnnResult)], street: Option[OSMStreetAndHouseNumber],
                          queryPoint: Point, address_tol_meters: Double = 20.0): ReverseGeocodingResponse = {

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
          streetResult.getDistanceAndNumber(queryPoint, address_tol_meters))
    }

  }

}