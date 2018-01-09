package it.agilelab.bigdata.gis.models

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence
import it.agilelab.bigdata.gis.models.OSMStreetType.OSMStreetType




object OSMStreetType extends Enumeration {
    type OSMStreetType = Value
    val Motorway, Secondary, Unclassified, Tertiary, Primary_link, Primary, Track, Residential, Pedestrian, Trunk_Link, Motorway_Link, Footway, Service, Path, Cycleway, Track_Grade2, Steps = Value
}



object OSMStreet{

}

case class OSMStreet(multiLineString: Geometry,
                     street: String,
                     code: String,
                     isBridge: Boolean,
                     isTunnel: Boolean,
                     speedLimit: Int,
                     bidirected: Boolean,
                     streetType: OSMStreetType)
  extends MultiLineString(
      {
          val length = multiLineString.getNumGeometries
          (0 until length).map{
              x => multiLineString.getGeometryN(x).asInstanceOf[LineString]
          }
      }.toArray,
      multiLineString.getFactory
  ) {

    override def toString() = {
        s"""Line:${multiLineString.toString()}
           |Street:${street}
           |SpeedLimit:${speedLimit}
           |Bidirected:${bidirected}
           |StreetType:${streetType}
           |code:${code}
           |isBridge:${isBridge}
           |isTunnel:${isTunnel}
         """.stripMargin
    }

    /** As seen from class Street, the missing signatures are as follows.
      *  For convenience, these are usable as stub implementations.
      */
//    def apply(filter: CoordinateFilter) = multiLineString.apply(filter)
//
//    def apply(filter: CoordinateSequenceFilter)  = multiLineString.apply(filter)
//
//    def apply(filter: GeometryFilter) = multiLineString.apply(filter)
//
//    def apply(filter: GeometryComponentFilter) = multiLineString.apply(filter)

    def getCoordinateSequence() = {
      new CoordinateArraySequence(getCoordinates)
    }

    override def computeEnvelopeInternal(): Envelope = {
        if (isEmpty)
            new Envelope
        else
          getCoordinateSequence.expandEnvelope(new Envelope)

    }

    override def getBoundary: Geometry = multiLineString.getBoundary

    override def compareToSameClass(o: scala.Any): Int = {
        val s: OSMStreet = o.asInstanceOf[OSMStreet]
        // MD - optimized implementation
        var i: Int = 0
        var j: Int = 0
        while (i < getNumPoints && j < s.getNumPoints) {
            val comparison: Int = getCoordinateSequence.getCoordinate(i).compareTo(s.getCoordinateSequence.getCoordinate(j))
            if (comparison != 0) return comparison
            i += 1
            j += 1
        }
        if (i < getNumPoints) {
            return 1
        }
        if (j < s.getNumPoints) {
            return -1
        }
        return 0
    }

    override def compareToSameClass(o: scala.Any, comp: CoordinateSequenceComparator): Int = {
        val s: OSMStreet = o.asInstanceOf[OSMStreet]
        return comp.compare(getCoordinateSequence(), s.getCoordinateSequence())
    }

    override def getCoordinates: Array[Coordinate] = multiLineString.getCoordinates

    override def getDimension: Int = multiLineString.getDimension

    override def getGeometryType: String = multiLineString.getGeometryType

    override def getBoundaryDimension: Int = multiLineString.getBoundaryDimension

    override def getCoordinate: Coordinate = multiLineString.getCoordinate

    override def isEmpty: Boolean = multiLineString.isEmpty

    override def normalize(): Unit = multiLineString.normalize()

    override def reverse(): Geometry = multiLineString.reverse()

    override def equalsExact(other: Geometry, tolerance: Double): Boolean = multiLineString.equalsExact(other, tolerance)

    override def getNumPoints: Int = multiLineString.getNumPoints



    import OSMStreetType._

    def isForCar = !isNotForCar

    def isNotForCar = streetType match {
        case Cycleway => true
        case _ => isForPedestrian
    }


    def isForPedestrian = streetType match {
        case Pedestrian | Footway | Steps => true
        case _ => false
    }




}