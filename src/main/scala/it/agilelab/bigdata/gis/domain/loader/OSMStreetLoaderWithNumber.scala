package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.domain.models.{OSMHouseNumber, OSMStreetAndHouseNumber}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

class OSMStreetLoaderWithNumber(houseNumbersIndex: (Geometry, String) => Seq[OSMHouseNumber]) extends OSMGenericStreetLoader {

  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] = {
    val notIndexedStreets: Iterator[OSMStreetAndHouseNumber] = loadObjects(houseNumbersIndex, sources: _*)
    buildIndex(notIndexedStreets.toList)
  }

  def loadObjects(houseNumbersIndex: (Geometry, String) => Seq[OSMHouseNumber], sources: String*): Iterator[OSMStreetAndHouseNumber] = {
    val lines: Iterator[OSMStreetAndHouseNumber] =
      sources
        .foldLeft(Seq.empty[OSMStreetAndHouseNumber].toIterator)((acc, source) =>
          acc ++ loadFile(source)
            .map(e => {
              val lr: Geometry = e._2
              val fields = e._1
              objectMappingWithAddresses(fields, lr, houseNumbersIndex)
            }))
    lines
  }

  protected def objectMappingWithAddresses(fields: Array[AnyRef],
                                           line: Geometry,
                                           houseNumbersIndex: (Geometry, String) => Seq[OSMHouseNumber]):
  OSMStreetAndHouseNumber = {
    val osmStreetWithoutNumbers: OSMStreetAndHouseNumber = objectMapping(fields, line)

    if (osmStreetWithoutNumbers.street.isDefined) { //In principle, this if should be useless...
      val houseNumbersIndexFuncResult: Seq[OSMHouseNumber] = houseNumbersIndex(line, osmStreetWithoutNumbers.street.get)
      OSMStreetAndHouseNumber.decorateWithNumbers(osmStreetWithoutNumbers, houseNumbersIndexFuncResult)
    } else {
      osmStreetWithoutNumbers
    }
  }

}
