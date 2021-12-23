package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.Configuration
import it.agilelab.gis.core.utils.GeocodeManagerUtils.{ filterPaths, BoundaryPathGroup, CountryPathSet, Path }

import java.io.File

private object Bound {
  val COUNTRY = "country"
  val COUNTY = "county"
  val REGION = "region"
  val CITY = "city"
}

/** [[GeocodePathManager]] provides path to shapefiles given a path to directory of a single country.
  * - postal codes shapefiles must end with 'gis-postalcode.shp'
  * - roads shapefiles must end with 'gis-roads.shp'
  * - house number shapefiles must end with 'gis-housenumbers.shp'
  *
  * @param conf configurations
  */
case class GeocodePathManager(conf: Config) extends Configuration {

  def getCountrySetting(countryName: String): CountrySettings =
    (for {
      countryConfig     <- read[Config](conf, countryName)
      countrySuffixList <- read[List[String]](countryConfig, Bound.COUNTRY)
      regionSuffixList  <- read[List[String]](countryConfig, Bound.REGION)
      countySuffixList  <- read[List[String]](countryConfig, Bound.COUNTY)
      citySuffixList    <- read[List[String]](countryConfig, Bound.CITY)
    } yield CountrySettings(countrySuffixList, regionSuffixList, countySuffixList, citySuffixList)).get

  def getCountryPathSet(countryFolder: File): CountryPathSet = {
    val boundaryPathGroup: BoundaryPathGroup = getBoundaryPathGroup(countryFolder)
    val postalCodesPath: Array[Path] = filterPaths(countryFolder, "postalcodes")
    val roadsPath: Array[Path] = filterPaths(countryFolder, "roads")
    val houseNumbersPath: Array[Path] = filterPaths(countryFolder, "housenumbers")

    CountryPathSet(boundaryPathGroup, postalCodesPath, roadsPath, houseNumbersPath)
  }

  private def getBoundaryPathGroup(countryFolder: File): BoundaryPathGroup = {
    val countrySettings: CountrySettings = getCountrySetting(countryFolder.getName)
    val paths: Array[Path] = countryFolder.listFiles().map(_.getAbsolutePath)

    val countryPathList: List[Path] =
      countrySettings.countrySuffixes.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))
    val regionPathList: List[Path] =
      countrySettings.regionSuffixes.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))
    val countyPathList: List[Path] =
      countrySettings.countySuffixes.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))
    val cityPathList: List[Path] =
      countrySettings.citySuffixes.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))

    BoundaryPathGroup(
      country = countryPathList,
      region = regionPathList,
      county = countyPathList,
      city = cityPathList
    )
  }
}

case class CountrySettings(
    countrySuffixes: List[String],
    regionSuffixes: List[String],
    countySuffixes: List[String],
    citySuffixes: List[String]
) {

  def clean: CountrySettings =
    CountrySettings(
      this.countrySuffixes.map(_.split('.').head),
      this.regionSuffixes.map(_.split('.').head),
      this.countySuffixes.map(_.split('.').head),
      this.citySuffixes.map(_.split('.').head)
    )
}
