package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.spatialList.GeometryList
import org.locationtech.jts.geom.OSMStreetAndHouseNumber

class OSMStreetLoaderWithoutNumber() extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] =
    buildIndex(loadObjects(sources: _*))

}
