package it.agilelab.bigdata.gis.domain.loader

import it.agilelab.bigdata.gis.domain.models.OSMStreetAndHouseNumber
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

class OSMEmptyStreetFileLoader extends OSMGenericStreetLoader {
  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] = null
}
