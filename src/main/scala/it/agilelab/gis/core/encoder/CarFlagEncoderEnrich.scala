package it.agilelab.gis.core.encoder

import com.graphhopper.reader.ReaderWay
import com.graphhopper.routing.profiles.UnsignedIntEncodedValue
import com.graphhopper.routing.util.{ CarFlagEncoder, EncodingManager, FlagEncoder }
import com.graphhopper.storage.IntsRef
import com.graphhopper.util.EdgeIteratorState
import it.agilelab.gis.core.utils.Logger

import java.util
import collection.JavaConverters._
import scala.collection.mutable

/** @author andreaL
  */
class CarFlagEncoderEnrich(speedBits: Int = 8, speedFactor: Double = 1, maxTurnCosts: Int = 0)
    extends CarFlagEncoder(speedBits, speedFactor, maxTurnCosts)
    with Logger {

  private final val unknownHighway = "unclassified"

  defaultSpeedMap.put("steps", 0)
  defaultSpeedMap.put("pedestrian", 0)
  defaultSpeedMap.put("footway", 0)
  defaultSpeedMap.put("path", 0)
  defaultSpeedMap.put("cycleway", 0)
  defaultSpeedMap.put("bridleway", 30)
  defaultSpeedMap.put("raceway", 90)
  defaultSpeedMap.put("escape", 40)
  defaultSpeedMap.put("busway", 50)
  defaultSpeedMap.put("bus_guideway", 50)

  // overrides values in CarFlagEncoder.java
  defaultSpeedMap.put("motorway", 110)
  defaultSpeedMap.put("motorway_link", 70)
  defaultSpeedMap.put("motorroad", 90)
  defaultSpeedMap.put("trunk", 100)
  defaultSpeedMap.put("trunk_link", 60)
  defaultSpeedMap.put("primary", 70)
  defaultSpeedMap.put("primary_link", 60)
  defaultSpeedMap.put("secondary", 60)
  defaultSpeedMap.put("secondary_link", 50)
  defaultSpeedMap.put("tertiary", 50)
  defaultSpeedMap.put("tertiary_link", 40)
  defaultSpeedMap.put("unclassified", 50)
  defaultSpeedMap.put("residential", 30)
  defaultSpeedMap.put("living_street", 5)
  defaultSpeedMap.put("service", 40)
  defaultSpeedMap.put("road", 50)
  defaultSpeedMap.put("track", 15)

  val highwayEncoder = new UnsignedIntEncodedValue("highway", 4, false)

  override def getAccess(way: ReaderWay): EncodingManager.Access =
    if (getHighwayValue(way) == 0) {
      EncodingManager.Access.CAN_SKIP
    } else {
      super.getAccess(way)
    }

  override def handleWayTags(
      edgeFlags: IntsRef,
      way: ReaderWay,
      access: EncodingManager.Access,
      relationFlags: Long
  ): IntsRef = {
    val value = super.handleWayTags(edgeFlags, way, access, relationFlags)
    highwayEncoder.setInt(false, value, getHighwayValue(way))
    value
  }

  private def getHighwayValue(way: ReaderWay): Int = {
    val highwayValue: String = way.getTag("highway")
    if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable"))
      0
    else
      HighwayType.indexOf(highwayValue)
  }

  /** Do not use within weighting as this is suboptimal from performance point of view.
    */
  def getHighwayAsString(edge: EdgeIteratorState): String = {
    val v: Int = getHighway(edge)

    HighwayType.fromId(v) match {
      case Some(value) => value.toString
      case None =>
        logger.warn(s"Highway $v not found in ${HighwayType.values.toList.mkString(",")}")
        unknownHighway
    }
  }

  def getHighway(edge: EdgeIteratorState): Int = highwayEncoder.getInt(false, edge.getFlags)

  override def applyMaxSpeed(way: ReaderWay, speed: Double): Double = getMaxSpeed(way) match {
    case max: Double if max >= 0 => max
    case _                       => speed
  }

  // Method copied from AbstractFlagEncoder that is overridden in CarFlagEncoder
  def getSpeedEncoderValue(reverse: Boolean, edgeFlags: IntsRef): Double = {
    val speedVal: Double = speedEncoder.getDecimal(reverse, edgeFlags)
    if (speedVal < 0) {
      throw new IllegalStateException("Speed was negative!? " + speedVal)
    }
    speedVal
  }
}

object HighwayType extends Enumeration {
  type HighwayType = Value

  val _default, motorway, motorway_link, motorroad, trunk, trunk_link, primary, primary_link, secondary, secondary_link,
      tertiary, tertiary_link, unclassified, residential, living_street, service, road, track, forestry, cycleway, path,
      footway, pedestrian, bus_guideway, escape, raceway, busway, bridleway, steps = Value

  def indexOf(value: String): Int = values.find(_.toString == value).map(_.id).getOrElse(0)
  def fromId(id: Int): Option[HighwayType] = values.find(_.id == id)

}
