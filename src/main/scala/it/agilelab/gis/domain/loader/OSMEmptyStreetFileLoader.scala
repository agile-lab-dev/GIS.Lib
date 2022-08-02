package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.spatialList.GeometryList
import org.locationtech.jts.geom.OSMStreetAndHouseNumber

class OSMEmptyStreetFileLoader extends OSMGenericStreetLoader {
  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] = null
}
