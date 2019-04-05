package it.agilelab.bigdata.gis.core.apps
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.{DataFlagEncoder, EncodingManager}
import com.graphhopper.routing.weighting.GenericWeighting
import com.graphhopper.util.PMap
import org.rogach.scallop._
import scalax.file.Path

/**
  * @author andreaL
  */
object ConverterFromOSMToGraphHopperMap extends App {
  args.foreach(println)

  case class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val graphLocation = opt[String](name = "graphLocation", required = true)
    val osmLocation = opt[String](name = "osmLocation", required = true)
    verify()
  }

  val conf = Conf(args)
  //We assume that there already is a stored graph in the folder. We delete those files
  val path: Path = Path.fromString(conf.graphLocation())
  //If false then method will throw an exception when encountering a file that cannot be deleted.  Otherwise it will continue
  //to delete all the files that can be deleted. This method is not transactional, all files visited before failure are deleted.
  //Try(path.deleteRecursively(continueOnFailure = false))
  //We create the graph object
  val hopperOSM = new GraphHopperOSM()

  //Set the the elevation flag to true to include 3d dimension
  hopperOSM.setElevation(true)
  //We use Generic Weighting with the DataFlagEncoder
  val encoder = new DataFlagEncoder()
  hopperOSM.setEncodingManager(new EncodingManager(encoder))
  val weighting = new GenericWeighting(encoder, new PMap())
  hopperOSM.getCHFactoryDecorator.addWeighting(weighting)

  //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
  hopperOSM.getCHFactoryDecorator.setEnabled(false)

  hopperOSM.setGraphHopperLocation(conf.graphLocation())
  //Set the .osm file and load directly from the map
  hopperOSM.setDataReaderFile(conf.osmLocation())
  hopperOSM.importOrLoad()
}