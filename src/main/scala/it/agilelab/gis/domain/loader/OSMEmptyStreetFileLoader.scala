package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.models.OSMStreetAndHouseNumber
import it.agilelab.gis.domain.spatialList.GeometryList

class OSMEmptyStreetFileLoader extends OSMGenericStreetLoader {
  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] = null
}
