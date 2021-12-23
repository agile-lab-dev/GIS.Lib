package it.agilelab.gis.domain.managers

import com.typesafe.config.Config
import it.agilelab.gis.core.utils.Configuration
import it.agilelab.gis.core.utils.GeoRelationManagerUtils.{ filterPaths, CountryPathSet, Path }

import java.io.File

/** [[GeoRelationPathManager]] provides path to shapefiles:
  *
  * - railways shapefiles must end with 'gis-railways.shp'
  *
  * - sea shapefiles must end with '.shp' inside the 'sea' folder
  */
object GeoRelationPathManager extends Configuration {

  def getCountryPathSet(countryFolder: File): CountryPathSet = {
    val railwaysPath: Array[Path] = filterPaths(countryFolder, "railways")

    CountryPathSet(railwaysPath)
  }

  def getSeaPathSet(maybeSeaPath: Option[String]): Array[Path] =
    maybeSeaPath match {
      case Some(seaPath) =>
        new File(seaPath)
          .listFiles()
          .map(_.getAbsolutePath)
          .filter(_.endsWith(".shp"))
      case None =>
        Array()
    }
}
