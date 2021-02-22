package it.agilelab.bigdata.gis.domain.loader

import it.agilelab.bigdata.gis.domain.models.OSMStreetAndHouseNumber
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

class OSMStreetLoaderWithoutNumber() extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] = {
    buildIndex(loadObjects(sources: _*))
  }

}
