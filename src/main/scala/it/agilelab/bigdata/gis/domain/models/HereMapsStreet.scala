package it.agilelab.bigdata.gis.domain.models

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence

trait HereMapsStreetType {
    def value: String
}

object HereMapsStreetType {

    case object MOTORWAY extends HereMapsStreetType {
        lazy val value = "motorway"
    }

    case object MAIN extends HereMapsStreetType {
        lazy val value = "main"
    }

    case object LOCAL_ACCESS extends HereMapsStreetType {
        lazy val value = "local access"
    }

    case object RESIDENTIAL extends HereMapsStreetType {
        lazy val value = "residential"
    }

    case object TRAIL extends HereMapsStreetType {
        lazy val value = "trail"
    }

    case object TRUCK extends HereMapsStreetType {
        lazy val value = "truck"
    }

    case object ND extends HereMapsStreetType {
        lazy val value = "N.D"
    }

    def fromValue(v: String): HereMapsStreetType =
        v.toLowerCase.trim match {
            case "1" => MOTORWAY
            case "2" => MAIN
            case "3" => LOCAL_ACCESS
            case "4" => RESIDENTIAL
            case "5" => TRAIL
            case "6" => TRUCK
            case _ => ND
        }
}

object HereMapsStreet{}

case class HereMapsStreet(lineString: Geometry,
                          street: String,
                          city: String,
                          county: String,
                          state: String,
                          country: String,
                          speedLimit: Int,
                          biDirected: Boolean,
                          length: Double,
                          streetType: HereMapsStreetType) extends Geometry(lineString.getFactory) {

    override def toString: String = {
        s"""Line: ${lineString.toString}
           |Street: $street
           |City: $city
           |County: $county
           |State: $state
           |Country: $country
           |FromSpeed: $speedLimit
           |Bidirected: $biDirected
           |Length: $length
           |StreetType: ${streetType.value}
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