package it.agilelab.bigdata.gis.domain.managers

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import it.agilelab.bigdata.gis.domain.managers.ManagerUtils.{BoundaryPathGroup, CountryPathSet, Path}

import scala.collection.JavaConversions._

private object Bound {

  val COUNTRY = "country"
  val COUNTY = "county"
  val REGION = "region"
  val CITY = "city"

}

object PathManager {

  private val conf: Config = ConfigFactory.load().getConfig("osm")

  def getInputPath = conf.getString("input_path")

  def getCountrySetting(countryName: String): CountrySettings = {
    val countryConfig: Config = conf.getConfig(countryName)

    val countrySuffixList: List[String] = countryConfig.getStringList(Bound.COUNTRY).toList
    val regionSuffixList: List[String] = countryConfig.getStringList(Bound.REGION).toList
    val countySuffixList: List[String] = countryConfig.getStringList(Bound.COUNTY).toList
    val citySuffixList: List[String] = countryConfig.getStringList(Bound.CITY).toList

    CountrySettings(
      countrySuffixList,
      regionSuffixList,
      countySuffixList,
      citySuffixList
    )
  }

  def getCountryPathSet(countryFolder: File): CountryPathSet = {

    val boundaryPathGroup: BoundaryPathGroup = getBoundaryPathGroup(countryFolder)

    val roadsPath: Array[Path] = countryFolder.listFiles().map(_.getAbsolutePath).filter(_.endsWith("gis-roads.shp"))

    val addressPath: Option[Path] = countryFolder.listFiles().map(_.getAbsolutePath).find(_.endsWith("addresses.shp"))

    CountryPathSet(boundaryPathGroup, roadsPath, addressPath)

  }

  private def getBoundaryPathGroup(countryFolder: File): BoundaryPathGroup = {

    val countryConfig: Config = conf.getConfig(countryFolder.getName)
    val countrySuffixList: List[String] = countryConfig.getStringList(Bound.COUNTRY).toList
    val regionSuffixList: List[String] = countryConfig.getStringList(Bound.REGION).toList
    val countySuffixList: List[String] = countryConfig.getStringList(Bound.COUNTY).toList
    val citySuffixList: List[String] = countryConfig.getStringList(Bound.CITY).toList

    val paths: Array[Path] = countryFolder.listFiles().map(_.getAbsolutePath)

    val countryPathList: List[Path] = countrySuffixList.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))
    val regionPathList: List[Path] = regionSuffixList.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))
    val countyPathList: List[Path] = countySuffixList.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))
    val cityPathList: List[Path] = citySuffixList.flatMap(validSuffix => paths.filter(_.endsWith(validSuffix)))

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
    CountrySettings(
      this.countrySuffixes.map(_.split('.').head),
      this.regionSuffixes.map(_.split('.').head),
      this.countySuffixes.map(_.split('.').head),
      this.citySuffixes.map(_.split('.').head)
    )
  }

}
