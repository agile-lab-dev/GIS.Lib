package it.agilelab.bigdata.gis.domain.managers

object ManagerUtils {

  type Path = String

  case class Paths(bounday: Array[Path], roads: Array[Path], addresses: Array[Path])

  case class BoundaryPathGroup(country: List[Path], region: List[Path], county: List[Path], city: List[Path])

  case class CountryPathSet(boundary: BoundaryPathGroup, roads: Array[Path], addressess: Option[Path])

  val FOLD_CITY_ON_COUNTY = 0
  val FOLD_COUNTY_ON_REGION = 1
  val FOLD_REGION_ON_COUNTRY = 2
  val DONE = 3

  //massima distanza per il knn.
  val NUMBERS_MAX_DISTANCE = 200D

  object BoundariesPos {
    val CITIES = 0
    val COUNTIES = 1
    val REGIONS = 2
    val COUNTRY = 3
  }

}
