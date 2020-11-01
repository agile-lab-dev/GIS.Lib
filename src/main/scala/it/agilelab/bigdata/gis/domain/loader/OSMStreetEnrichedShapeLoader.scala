package it.agilelab.bigdata.gis.domain.loader

import com.vividsolutions.jts.geom.{Geometry, MultiLineString}
import it.agilelab.bigdata.gis.core.loader.Loader
import it.agilelab.bigdata.gis.domain.models.{OSMAddress, OSMSmallAddressNumber, OSMStreetEnriched, OSMStreetType}
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

import scala.util.Try
/**
 * @author Gloria Lovera
 */
object OSMStreetEnrichedShapeLoader{
  //Pay attention to side effects
  var index: GeometryList[OSMStreetEnriched] = null
  def getStreetIndex(path: String) = {
    if (index == null){
      index = new OSMStreetEnrichedShapeLoader().loadIndex(path)
    }
    index
  }
}
class OSMStreetEnrichedShapeLoader() extends Loader[OSMStreetEnriched]{
  def loadIndex(addressesIndex: (Geometry,String) => Seq[OSMAddress], sources: String*): GeometryList[OSMStreetEnriched] = {
    val a = loadObjects(addressesIndex, sources:_*)
    val aList = a.toList
    buildIndex(aList)
  }

  def loadObjects(addressesIndex: (Geometry,String) => Seq[OSMAddress], sources: String*): Iterator[OSMStreetEnriched] = {
    val lines: Iterator[OSMStreetEnriched] =
      sources
        .foldLeft(Seq.empty[OSMStreetEnriched].toIterator)((acc, source) =>
          acc ++ loadFile(source)
            .map(e => {
              val lr: Geometry = e._2
              val fields = e._1
              objectMapping(fields, lr, addressesIndex)
            }))
    lines
  }

  override def loadFile(source: String): Iterator[(Array[AnyRef], Geometry)] = {

    val lines: Iterator[(Array[AnyRef], MultiLineString)] =
      ShapeFileReader.readMultiLineFeatures(source)
        .map(e => (e._2.toArray, e._1)).toIterator

    lines
  }
  protected def objectMapping(fields: Array[AnyRef], line: Geometry, addressesIndex: (Geometry,String) => Seq[OSMAddress]): OSMStreetEnriched = {
    val osmStreetWithoutNumbers: OSMStreetEnriched = objectMapping(fields, line)

    if(osmStreetWithoutNumbers.street.isDefined) { //In principle, this if should be useless...
      val addressesIndexeFuncResult = addressesIndex(line, osmStreetWithoutNumbers.street.get)
      OSMStreetEnriched.decorateWithNumbers(osmStreetWithoutNumbers, addressesIndexeFuncResult)
    }

    else
      osmStreetWithoutNumbers
  }
  protected def objectMapping(fields: Array[AnyRef],line: Geometry): OSMStreetEnriched = {
    val street = Try(fields(4).toString).toOption
    val streetType = Try(if(fields(3) != null) fields(3).toString else "").toOption
    val st: Option[OSMStreetType.Value] =
      streetType
        .map(_.toLowerCase)
        .map {
          case "motorway" => OSMStreetType.Motorway
          case "secondary" => OSMStreetType.Secondary
          case "unclassified" => OSMStreetType.Unclassified
          case "tertiary" => OSMStreetType.Tertiary
          case "primary_link" => OSMStreetType.Primary_link
          case "primary" => OSMStreetType.Primary
          case "track" => OSMStreetType.Track
          case "residential" => OSMStreetType.Residential
          case "pedestrian" => OSMStreetType.Pedestrian
          case "trunk_link" => OSMStreetType.Trunk_Link
          case "motorway_link" => OSMStreetType.Motorway_Link
          case "footway" => OSMStreetType.Footway
          case "service" => OSMStreetType.Service
          case "path" => OSMStreetType.Path
          case "cycleway" => OSMStreetType.Cycleway
          case "track_grade2" => OSMStreetType.Track_Grade2
          case "steps" => OSMStreetType.Steps
          case _ => OSMStreetType.Unclassified
        }

    val pointsArray = line.getCoordinates.flatMap(coord => Array(coord.x, coord.y))

    OSMStreetEnriched(pointsArray, street, st, Seq.empty[OSMSmallAddressNumber])
  }
}


