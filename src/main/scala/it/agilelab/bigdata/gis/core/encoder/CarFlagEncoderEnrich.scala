package it.agilelab.bigdata.gis.core.encoder

import java.util

import com.graphhopper.reader.ReaderWay
import com.graphhopper.routing.profiles.{EncodedValue, SimpleIntEncodedValue}
import com.graphhopper.routing.util.{CarFlagEncoder, EncodingManager}
import com.graphhopper.storage.IntsRef
import com.graphhopper.util.EdgeIteratorState

/**
 * @author andreaL
 */

class CarFlagEncoderEnrich(speedBits: Int = 5, speedFactor: Double = 5, maxTurnCosts: Int = 0)
  extends CarFlagEncoder(speedBits, speedFactor, maxTurnCosts) {

  lazy val highwayList: Seq[String] = Seq(
    /* reserve index=0 for unset roads (not accessible or not mapping) */
    "_default", "motorway", "motorway_link", "motorroad", "trunk", "trunk_link", "primary",
    "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified",
    "residential", "living_street", "service", "road", "track", "forestry", "cycleway",
    "path", "footway", "pedestrian")

  val highwayMap: Map[String, Int] = highwayList.zipWithIndex.toMap
  val highwayReverseMap: Map[Int, String] = for ((k,v) <- highwayMap) yield (v, k)

  private val highwayEncoder: SimpleIntEncodedValue =
    new SimpleIntEncodedValue("car.highway", 6, false)

  defaultSpeedMap.put("steps", 0)
  defaultSpeedMap.put("pedestrian", 0)
  defaultSpeedMap.put("footway", 0)
  defaultSpeedMap.put("path", 0)
  defaultSpeedMap.put("cycleway", 0)

  override def createEncodedValues(registerNewEncodedValue: util.List[EncodedValue], prefix: String, index: Int): Unit = {
    super.createEncodedValues(registerNewEncodedValue, prefix, index)
    registerNewEncodedValue.add(highwayEncoder)
  }

  /**
   * Do not use within weighting as this is suboptimal from performance point of view.
   */
  def getHighwayAsString(edge: EdgeIteratorState): String = {
    val reverseKey: Int = highwayEncoder.getInt(false, edge.getFlags)
    highwayReverseMap(reverseKey)

  }

  override def handleWayTags(edgeFlags: IntsRef, readerWay: ReaderWay, accept: EncodingManager.Access, relationFlags: Long): IntsRef = {
    super.handleWayTags(edgeFlags, readerWay, accept, relationFlags)
    if (readerWay.hasTag("highway")) {
      highwayMap.get(readerWay.getTag("highway")) match {
        case None => highwayEncoder.setInt(false, edgeFlags, 0)
        case Some(x) => highwayEncoder.setInt(false, edgeFlags, x)
      }
    }
    else
      highwayEncoder.setInt(false, edgeFlags, 0)

    edgeFlags
  }


}
