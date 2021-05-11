package it.agilelab.bigdata.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.bigdata.gis.core.utils.Configuration
import it.agilelab.bigdata.gis.core.utils.ManagerUtils.{ BoundaryPathGroup, CountryPathSet, Path }
import it.agilelab.bigdata.gis.domain.configuration.PathManagerConfiguration

import java.io.File

private object Bound {
  val COUNTRY = "country"
  val COUNTY = "county"
  val REGION = "region"
  val CITY = "city"
}

case class PathManager(conf: Config) extends Configuration {

  val pathConfig: PathManagerConfiguration = PathManagerConfiguration(conf)

  def getCountrySetting(countryName: String): CountrySettings =
    (for {
      countryConfig     <- read[Config](conf, countryName)
      countrySuffixList <- read[List[String]](countryConfig, Bound.COUNTRY)
      regionSuffixList  <- read[List[String]](countryConfig, Bound.REGION)
      countySuffixList  <- read[List[String]](countryConfig, Bound.COUNTY)
      citySuffixList    <- read[List[String]](countryConfig, Bound.CITY)
    } yield CountrySettings(countrySuffixList, regionSuffixList, countySuffixList, citySuffixList)).get

  def getCountryPathSet(countryFolder: File): CountryPathSet = {
//    val boundaryPathGroup: BoundaryPathGroup = getBoundaryPathGroup(countryFolder)
    val postalCodesPath: Array[Path] =
      countryFolder.listFiles().map(_.getAbsolutePath).filter(_.matches(".*postalcodes.*.shp"))
    val roadsPath: Array[Path] = countryFolder.listFiles().map(_.getAbsolutePath).filter(_.matches(".*roads.*.shp"))
    val houseNumbersPath: Array[Path] =
      countryFolder.listFiles().map(_.getAbsolutePath).filter(_.matches(".*housenumbers.*.shp"))
    val adminLevels: Array[Path] =
      countryFolder.listFiles().map(_.getAbsolutePath).filter(_.matches(".*admin.*.shp"))
    CountryPathSet(adminLevels, postalCodesPath, roadsPath, houseNumbersPath)
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

  def clean: CountrySettings = {
    // Take only admin level from a string like "8.shp" => "8"
    CountrySettings(
      this.countrySuffixes.map(_.split('.').head),
      this.regionSuffixes.map(_.split('.').head),
      this.countySuffixes.map(_.split('.').head),
      this.citySuffixes.map(_.split('.').head)
    )
  }
}
