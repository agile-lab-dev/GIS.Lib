package it.agilelab.bigdata.gis.domain.loader

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.{Configuration, Logger, ObjectPickler}
import it.agilelab.bigdata.gis.domain.managers.IndexManager
import it.agilelab.bigdata.gis.domain.models.{OSMBoundary, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import scala.util.{Failure, Success, Try}

case class OSMManagerConfiguration(inputPath: List[String],
                                   vehicle:String,
                                   filterEmptyStreets: Boolean,
                                   roadTolMeters:Double,
                                   addressTolMeters: Double,
                                   boundariesGeometryList: GeometryList[OSMBoundary],
                                   regionGeometryList: GeometryList[OSMBoundary],
                                   roadsGeometryList: GeometryList[OSMStreetAndHouseNumber]
                                  )

object OSMManagerConfiguration extends Configuration with Logger {

  private val OSM: String = "osm"
  private val INPUT_PATHS : String = "input_paths"
  private val VEHICLE : String = "vehicle"
  private val FILTER_EMPTY_STREETS : String = "filter_empty_streets"
  private val READ_TOL_METERS : String = "road_tol_meters"
  private val ADDRESS_TOL_METERS : String = "address_tol_meters"

  def apply(config: Config): OSMManagerConfiguration = {

    val parsedConfig: Try[OSMManagerConfiguration] = for {
      osm <- read[Config](config, OSM)
      inputPaths <- read[List[String]](osm,INPUT_PATHS)
      vehicle <- read[String](osm,VEHICLE)
      filterEmptyStreets <- read[Boolean](osm, FILTER_EMPTY_STREETS)
      roadTolMeters <- read[Double](osm, READ_TOL_METERS)
      addressTolMeters <- read[Double](osm, ADDRESS_TOL_METERS)

      (boundariesGeometryList, regionGeometryList, roadsGeometryList) <- Try {
        if (inputPaths.length == 1) {
          val index = IndexManager.makeIndices(inputPaths.head)
          (index.boundaries, index.regions, index.streets)
        } else {
          val boundaries = ObjectPickler.unpickle[GeometryList[OSMBoundary]](inputPaths.head)
          val regions = ObjectPickler.unpickle[GeometryList[OSMBoundary]](inputPaths(1))
          val streets = ObjectPickler.unpickle[GeometryList[OSMStreetAndHouseNumber]](inputPaths(2))
          (boundaries, regions, streets)
        }
      }.recoverWith { case exception: Exception =>
          logger.error(s"Error calculating boundaries, regions or streets. Check if $INPUT_PATHS are set correctly.")
          Failure(exception)
      }
    } yield OSMManagerConfiguration(inputPaths, vehicle, filterEmptyStreets, roadTolMeters, addressTolMeters, boundariesGeometryList, regionGeometryList, roadsGeometryList)

    parsedConfig match {
      case Failure(exception)     => throw exception
      case Success(configuration) => configuration
    }
  }
}