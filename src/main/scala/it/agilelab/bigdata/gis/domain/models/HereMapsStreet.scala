package it.agilelab.bigdata.gis.domain.models

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence
import it.agilelab.bigdata.gis.domain.models.HereMapsStreetType.HereMapsStreetType

object HereMapsStreetType extends Enumeration {
    type HereMapsStreetType = Value
    val Motorway, ExtraUrban, Area1_Large, Area2_Medium, Area3_Small, Unknown = Value
}



object HereMapsStreet{

}

case class HereMapsStreet(lineString: Geometry, street: String, city: String, county: String, state: String, country: String, speedLimit: Int, bidirected: Boolean, length: Double, streetType: HereMapsStreetType) extends Geometry(lineString.getFactory) {

    override def toString: String = {
        s"""Line: ${lineString.toString}
           |Street: $street
           |City: $city
           |County: $county
           |State: $state
           |Country: $country
           |FromSpeed: $speedLimit
           |Bidirected: $bidirected
           |Length: $length
           |StreetType: $streetType
         """.stripMargin
    }

    /** As seen from class Street, the missing signatures are as follows.
      *  For convenience, these are usable as stub implementations.
      */
    def apply(filter: CoordinateFilter): Unit = lineString.apply(filter)

    def apply(filter: CoordinateSequenceFilter): Unit = lineString.apply(filter)

    def apply(filter: GeometryFilter): Unit = lineString.apply(filter)

    def apply(filter: GeometryComponentFilter): Unit = lineString.apply(filter)

    override def computeEnvelopeInternal(): Envelope = {
        if (isEmpty)
            new Envelope
        else
            getCoordinateSequence.expandEnvelope(new Envelope)

    }

    override def getBoundary: Geometry = lineString.getBoundary

    override def compareToSameClass(o: scala.Any): Int = {
        val s: HereMapsStreet = o.asInstanceOf[HereMapsStreet]
        // MD - optimized implementation
        var i: Int = 0
        var j: Int = 0
        while (i < getNumPoints && j < s.getNumPoints) {
            val comparison: Int =
                getCoordinateSequence.getCoordinate(i).compareTo(s.getCoordinateSequence.getCoordinate(j))
            if (comparison != 0) return comparison
            i += 1
            j += 1
        }

        if (i < getNumPoints) 1
        else if (j < s.getNumPoints) -1
        else 0
    }

    override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
        val s: HereMapsStreet = o.asInstanceOf[HereMapsStreet]
        comp.compare(getCoordinateSequence, s.getCoordinateSequence)
    }

    def getCoordinateSequence: CoordinateArraySequence = {
        new CoordinateArraySequence(getCoordinates)
    }

    override def getCoordinates: Array[Coordinate] = lineString.getCoordinates

    override def getDimension: Int = lineString.getDimension

    override def getGeometryType: String = lineString.getGeometryType

    override def getBoundaryDimension: Int = lineString.getBoundaryDimension

    override def getCoordinate: Coordinate = lineString.getCoordinate

    override def isEmpty: Boolean = lineString.isEmpty

    override def normalize(): Unit = lineString.normalize()

    override def reverse(): Geometry = lineString.reverse()

    override def equalsExact(other: Geometry, tolerance: Double): Boolean = lineString.equalsExact(other, tolerance)

    override def getNumPoints: Int = lineString.getNumPoints
}