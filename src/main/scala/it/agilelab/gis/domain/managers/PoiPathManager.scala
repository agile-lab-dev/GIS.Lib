package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.Configuration
import it.agilelab.gis.core.utils.GeocodeManagerUtils.{ filterPaths, Path }

import java.io.File

/** [[PoiPathManager]] provides path to shapefiles given a path to directory of a single country.
  * - amenity shapefiles must be inside a folder which name is in the config with key: amenity.folderName
  * - landuse shapefiles must be inside a folder which name is in the config with key: landuse.folderName
  * - leisure shapefiles must be inside a folder which name is in the config with key: leisure.folderName
  * - natural shapefiles must be inside a folder which name is in the config with key: natural.folderName
  * - shop shapefiles must be inside a folder which name is in the config with key: shop.folderName
  *
  * @param conf configurations
  */
case class PoiPathManager(conf: Config) extends Configuration {

  def getCountryPathSet(countryFolder: File): PoiPathSet = {
    val amenityPath: Array[Path] = filterPaths(
      countryFolder.toPath.resolve(conf.getString("amenity.folderName")).toFile,
      conf.getString("amenity.regexFilterFileNames"))
    val landusePath: Array[Path] = filterPaths(
      countryFolder.toPath.resolve(conf.getString("landuse.folderName")).toFile,
      conf.getString("landuse.regexFilterFileNames"))
    val leisurePath: Array[Path] = filterPaths(
      countryFolder.toPath.resolve(conf.getString("leisure.folderName")).toFile,
      conf.getString("leisure.regexFilterFileNames"))
    val naturalPath: Array[Path] = filterPaths(
      countryFolder.toPath.resolve(conf.getString("natural.folderName")).toFile,
      conf.getString("natural.regexFilterFileNames"))
    val shopPath: Array[Path] = filterPaths(
      countryFolder.toPath.resolve(conf.getString("shop.folderName")).toFile,
      conf.getString("shop.regexFilterFileNames"))
    PoiPathSet(amenityPath, landusePath, leisurePath, naturalPath, shopPath)
  }

}

case class PoiPathSet(
    amenityPath: Array[Path],
    landusePath: Array[Path],
    leisurePath: Array[Path],
    naturalPath: Array[Path],
    shopPath: Array[Path]
)
