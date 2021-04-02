package it.agilelab.bigdata.gis.core.apps
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.weighting.FastestWeighting
import com.graphhopper.util.PMap
import it.agilelab.bigdata.gis.core.encoder.CarFlagEncoderEnrich
import it.agilelab.bigdata.gis.core.utils.Logger
import org.rogach.scallop._

/**
  * @author andreaL
  */
object ConverterFromOSMToGraphHopperMap extends App with Logger {

  case class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val graphLocation: ScallopOption[String] = opt[String](name = "graphLocation", required = true)
    val osmLocation: ScallopOption[String] = opt[String](name = "osmLocation", required = true)
    verify()
  }

  logger.info("Arguments", args.mkString(" "))

  val conf: Conf = Conf(args)

  //We create the graph object
  val hopperOSM = new GraphHopperOSM()

  //Set the the elevation flag to true to include 3d dimension
  hopperOSM.setElevation(true)
  //We use Generic Weighting with the DataFlagEncoder
  val encoder = new CarFlagEncoderEnrich()
  hopperOSM.setEncodingManager(new EncodingManager(encoder))
  hopperOSM.setElevation(true)
  val weighting = new FastestWeighting(encoder, new PMap())
  hopperOSM.getCHFactoryDecorator.addWeighting(weighting)

  //We disable the contraction hierarchies post processing. It seems to be mandatory in order to do map matching
  hopperOSM.getCHFactoryDecorator.setEnabled(false)

  hopperOSM.setGraphHopperLocation(conf.graphLocation())
  //Set the .osm file and load directly from the map
  hopperOSM.setDataReaderFile(conf.osmLocation())
  hopperOSM.importOrLoad()
}