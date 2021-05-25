package it.agilelab.gis.core.encoder

import com.graphhopper.reader.ReaderWay
import com.graphhopper.routing.util.{ CarFlagEncoder, EncodedValue }
import com.graphhopper.util.EdgeIteratorState
import it.agilelab.gis.core.utils.Logger

import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable

/** @author andreaL
  */
class CarFlagEncoderEnrich(speedBits: Int = 8, speedFactor: Double = 1, maxTurnCosts: Int = 0)
    extends CarFlagEncoder(speedBits, speedFactor, maxTurnCosts)
    with Logger {

  final private val highwayMap: util.Map[String, Integer] = new util.HashMap[String, Integer]
  final private val highwayMapIndex: mutable.Map[Int, String] = mutable.Map()

  private final val unknownHighway = "unclassified"

  private var highwayEncoder: EncodedValue = _

  defaultSpeedMap.put("steps", 0)
  defaultSpeedMap.put("pedestrian", 0)
  defaultSpeedMap.put("footway", 0)
  defaultSpeedMap.put("path", 0)
  defaultSpeedMap.put("cycleway", 0)
  defaultSpeedMap.put("bridleway", 30)
  defaultSpeedMap.put("raceway", 90)
  defaultSpeedMap.put("escape", 30)
  defaultSpeedMap.put("busway", 45)
  defaultSpeedMap.put("bus_guideway", 45)

  val highwayList: Seq[String] = Seq(
    /* reserve index=0 for unset roads (not accessible) */
    "_default",
    "motorway",
    "motorway_link",
    "motorroad",
    "trunk",
    "trunk_link",
    "primary",
    "primary_link",
    "secondary",
    "secondary_link",
    "tertiary",
    "tertiary_link",
    "unclassified",
    "residential",
    "living_street",
    "service",
    "road",
    "track",
    "forestry",
    "cycleway",
    "path",
    "footway",
    "pedestrian",
    "bus_guideway",
    "escape",
    "raceway",
    "busway",
    "bridleway",
    "steps"
  )

  logger.info(highwayList.diff(defaultSpeedMap.keySet().toSeq).toString())

  highwayList.zipWithIndex.foreach { case (value, idx) =>
    highwayMap.put(value, idx)
    highwayMapIndex.put(idx, value)
  }

  override def acceptWay(way: ReaderWay): Long =
    // important to skip unsupported highways, otherwise too many have to be removed after graph creation
    // and node removal is not yet designed for that
    if (getHighwayValue(way) == 0) 0
    else acceptBit

  private def getHighwayValue(way: ReaderWay): Int = {
    val highwayValue: String = way.getTag("highway")

    if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable"))
      0
    else
      Option(highwayMap.get(highwayValue).asInstanceOf[Int]).getOrElse(0)
  }

  override def handleWayTags(way: ReaderWay, allowed: Long, relationFlags: Long): Long = {
    val hwValue = getHighwayValue(way)
    highwayEncoder.setValue(super.handleWayTags(way, allowed, relationFlags), hwValue)
  }

  override def defineWayBits(index: Int, shift: Int): Int = {
    // first bits are reserved for route handling in superclass
    val _shift: Int = super.defineWayBits(index, shift)

    highwayEncoder = new EncodedValue("highway", _shift, speedBits, speedFactor, 0, highwayMap.size, true)

    _shift + highwayEncoder.getBits
  }

  def getHighway(edge: EdgeIteratorState): Int = highwayEncoder.getValue(edge.getFlags).toInt

  /** Do not use within weighting as this is suboptimal from performance point of view.
    */
  def getHighwayAsString(edge: EdgeIteratorState): String = {
    val v: Int = getHighway(edge)

    highwayMapIndex.get(v) match {
      case Some(value) => value
      case None =>
        logger.warn(s"Highway $v not found in ${highwayMap.mkString(",")}")
        unknownHighway
    }
  }

  /*

  Override reasoning: CarFlagEncoder.applyMaxSpeed implementation is:

        double maxSpeed = getMaxSpeed(way);
        // We obey speed limits
        if (maxSpeed >= 0) {
            // We assume that the average speed is 90% of the allowed maximum
            return maxSpeed * 0.9;
        }
        return speed;

   That 0.9 factor is not what we want.
   */
  override def applyMaxSpeed(way: ReaderWay, speed: Double): Double = getMaxSpeed(way) match {
    case max: Double if max >= 0 => max
    case _                       => speed
  }
}
