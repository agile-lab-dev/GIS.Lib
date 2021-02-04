package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.domain.models.{OSMAddress, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

class OSMStreetLoaderWithNumber(addressesIndex: (Geometry,String) => Seq[OSMAddress]) extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] = {
    val notIndexedStreets: Iterator[OSMStreetAndHouseNumber] = loadObjects(addressesIndex, sources:_*)
    buildIndex(notIndexedStreets.toList)
  }

  def loadObjects(addressesIndex: (Geometry,String) => Seq[OSMAddress], sources: String*): Iterator[OSMStreetAndHouseNumber] = {
    val lines: Iterator[OSMStreetAndHouseNumber] =
      sources
        .foldLeft(Seq.empty[OSMStreetAndHouseNumber].toIterator)((acc, source) =>
          acc ++ loadFile(source)
            .map(e => {
              val lr: Geometry = e._2
              val fields = e._1
              objectMappingWithAddresses(fields, lr, addressesIndex)
            }))
    lines
  }

  protected def objectMappingWithAddresses(fields: Array[AnyRef], line: Geometry, addressesIndex: (Geometry,String) => Seq[OSMAddress]): OSMStreetAndHouseNumber = {
    val osmStreetWithoutNumbers: OSMStreetAndHouseNumber = objectMapping(fields, line)

    if(osmStreetWithoutNumbers.street.isDefined) { //In principle, this if should be useless...
      val addressesIndexeFuncResult = addressesIndex(line, osmStreetWithoutNumbers.street.get)
      OSMStreetAndHouseNumber.decorateWithNumbers(osmStreetWithoutNumbers, addressesIndexeFuncResult)
    }

    else
      osmStreetWithoutNumbers
  }

}
