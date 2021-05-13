package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.models.OSMStreetAndHouseNumber
import it.agilelab.gis.domain.spatialList.GeometryList

class OSMStreetLoaderWithoutNumber() extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] =
    buildIndex(loadObjects(sources: _*))

}
