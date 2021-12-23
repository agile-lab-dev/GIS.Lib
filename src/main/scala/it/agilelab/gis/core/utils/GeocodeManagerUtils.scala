package it.agilelab.gis.core.utils

object GeocodeManagerUtils extends ManagerUtils {

  case class Paths(boundary: Array[Path], postalCodes: Array[Path], roads: Array[Path], houseNumbers: Array[Path])

  case class BoundaryPathGroup(country: List[Path], region: List[Path], county: List[Path], city: List[Path])

  case class CountryPathSet(
      boundary: BoundaryPathGroup,
      postalCodes: Array[Path],
      roads: Array[Path],
      houseNumbers: Array[Path]
  )

  val FOLD_CITY_ON_COUNTY = 0
  val FOLD_COUNTY_ON_REGION = 1
  val FOLD_REGION_ON_COUNTRY = 2
  val DONE = 3

  //maximum distance for kNN.
  val NUMBERS_MAX_DISTANCE = 200d
}
