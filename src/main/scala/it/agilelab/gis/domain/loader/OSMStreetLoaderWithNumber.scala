package it.agilelab.gis.domain.loader

import it.agilelab.gis.core.utils.Logger
import it.agilelab.gis.domain.spatialList.GeometryList
import org.locationtech.jts.geom.{ Geometry, OSMHouseNumber, OSMStreetAndHouseNumber }

class OSMStreetLoaderWithNumber(houseNumbersIndex: (Geometry, String) => Seq[OSMHouseNumber])
    extends OSMGenericStreetLoader
    with Logger {

  override def loadIndex(sources: String*): GeometryList[OSMStreetAndHouseNumber] =
    buildIndex(loadObjects(houseNumbersIndex, sources: _*).toList)

  def loadObjects(
      houseNumbersIndex: (Geometry, String) => Seq[OSMHouseNumber],
      sources: String*
  ): Seq[OSMStreetAndHouseNumber] = {
    logger.info("Load objects of {} sources", sources.size)
    sources
      .foldLeft(Seq.empty[OSMStreetAndHouseNumber]) { (acc, source) =>
        logger.info("Loading source {}", source)
        val start = System.currentTimeMillis()
        val raw = loadFile(source).toSeq
        logger.info("Source {} contains {} elements", source, raw.size)
        val r = acc ++ raw.par
          .map { e =>
            val lr: Geometry = e._2
            val fields = e._1
            val r = objectMappingWithAddresses(fields, lr, houseNumbersIndex)
            System.gc()
            r
          }
        logger.info("Loaded objects of source {} in {} ms", source, System.currentTimeMillis() - start)
        r
      }
  }

  protected def objectMappingWithAddresses(
      fields: Array[AnyRef],
      line: Geometry,
      houseNumbersIndex: (Geometry, String) => Seq[OSMHouseNumber]
  ): OSMStreetAndHouseNumber = {
    val osmStreetWithoutNumbers: OSMStreetAndHouseNumber = objectMapping(fields, line)

    if (osmStreetWithoutNumbers.street.isDefined) { //In principle, this if should be useless...
      val houseNumbersIndexFuncResult: Seq[OSMHouseNumber] = houseNumbersIndex(line, osmStreetWithoutNumbers.street.get)
      OSMStreetAndHouseNumber.decorateWithNumbers(osmStreetWithoutNumbers, houseNumbersIndexFuncResult)
    } else {
      osmStreetWithoutNumbers
    }
  }

}
