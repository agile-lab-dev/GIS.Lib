package it.agilelab.bigdata.gis.core.encoder

import java.util

import com.graphhopper.reader.ReaderWay
import com.graphhopper.routing.util.{CarFlagEncoder, EncodedDoubleValue, EncodedValue}
import com.graphhopper.util.EdgeIteratorState

import scala.collection.JavaConversions._

/**
 * @author andreaL
 */
class CarFlagEncoderEnrich(speedBits: Int = 5, speedFactor: Double = 5, maxTurnCosts: Int = 0) extends CarFlagEncoder(speedBits, speedFactor, maxTurnCosts) {

  final private val highwayMap: util.Map[String, Integer] = new util.HashMap[String, Integer]
  private var highwayEncoder: EncodedValue = _

  defaultSpeedMap.put("steps", 0)
  defaultSpeedMap.put("pedestrian", 0)
  defaultSpeedMap.put("footway", 0)
  defaultSpeedMap.put("path", 0)
  defaultSpeedMap.put("cycleway", 0)

  val highwayList: Seq[String] = Seq(
      /* reserve index=0 for unset roads (not accessible) */
      "_default", "motorway", "motorway_link", "motorroad", "trunk", "trunk_link", "primary",
      "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified",
      "residential", "living_street", "service", "road", "track", "forestry", "cycleway",
      "path", "footway", "pedestrian")

  highwayList.zipWithIndex.foreach { case (value, idx) =>
    highwayMap.put(value, idx)
  }


  override def acceptWay(way: ReaderWay): Long = {
    // important to skip unsupported highways, otherwise too many have to be removed after graph creation
    // and node removal is not yet designed for that
    if (getHighwayValue(way) == 0) 0
    else acceptBit
  }

  private def getHighwayValue(way: ReaderWay): Int = {
    val highwayValue: String = way.getTag("highway")

    if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable"))
      0
    else
      Option(highwayMap.get(highwayValue).asInstanceOf[Int]).getOrElse(0)
  }

  override def defineWayBits(index: Int, shift: Int): Int = {
    // first two bits are reserved for route handling in superclass

    val _shift: Int = super.defineWayBits(index, shift)
    speedEncoder =
      new EncodedDoubleValue(
        "Speed",
        _shift,
        speedBits,
        speedFactor,
        defaultSpeedMap.get("secondary").asInstanceOf[Int],
        maxPossibleSpeed
      )

    highwayEncoder =
      new EncodedValue("highway", _shift, 5, 1, 0, highwayMap.size, true)

    _shift + highwayEncoder.getBits
  }

  def getHighway(edge: EdgeIteratorState): Int = highwayEncoder.getValue(edge.getFlags).toInt

  /**
   * Do not use within weighting as this is suboptimal from performance point of view.
   */
  def getHighwayAsString(edge: EdgeIteratorState): String = {
    val v: Int = getHighway(edge)

    highwayMap.entrySet().find(e => e.getValue == v) match {
      case Some(value) => value.getKey
      case None => null
    }

  }

  private def getHighwaySpeedMap(map: util.Map[String, Double]): Seq[Double] = {

    if (map == null)
      throw new IllegalArgumentException("Map cannot be null when calling getHighwaySpeedMap")

    map.entrySet().map(e => {
      val key: Integer = highwayMap.get(e.getKey)
      if (key == null) throw new IllegalArgumentException("Graph not prepared for highway=" + e.getKey)
      if (e.getValue < 0) throw new IllegalArgumentException("Negative speed " + e.getValue + " not allowed. highway=" + e.getKey)
      e.getValue
    }).toSeq

  }
}
