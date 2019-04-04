/**
 * FILE: SpatialRDD.java
 * PATH: org.datasyslab.geospark.spatialRDD.SpatialRDD.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.domain.spatialList

import java.io.Serializable

import com.vividsolutions.jts.geom.{Envelope, Geometry, GeometryFactory}
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.quadtree.Quadtree
import com.vividsolutions.jts.index.strtree.STRtree
import it.agilelab.bigdata.gis.core.model.IndexType
import it.agilelab.bigdata.gis.core.model.geometry.Circle
import it.agilelab.bigdata.gis.core.utils.{XMaxComparator, XMinComparator, YMaxComparator, YMinComparator}


// TODO: Auto-generated Javadoc

/**
 * The Class SpatialList.
 */
abstract class SpatialList extends Serializable{
	
	/** The Constant logger. */
		//val logger = Logger.getLogger(this.getClass)
    
    /** The total number of records. */
    var totalNumberOfRecords: Int = -1
    
    /** The boundary. */
    private var _boundary = new Array[Double](4)
    
    /** The boundary envelope. */
    private var boundaryEnvelope: Envelope  = _

    /** The indexed Collection. */
    var indexedCollection: Map[Integer, Object] = _
    
    /** The indexed raw RDD. */
    var index: SpatialIndex = _

    /** The raw spatial RDD. */
    var rawSpatialCollection: List[Object] = _

	/** The grids. */
    var grids: List[Envelope] = _
    

	/**
	 * Count without duplicates.
	 *
	 * @return the long
	 */
	def countWithoutDuplicates(): Long =
		rawSpatialCollection.toSet.size


	/**
	 * Builds the index.
	 *
	 * @param indexType the index type
	 * @throws Exception the exception
	 */
	def buildIndex(indexType: IndexType): Unit = {

		val rt =
      indexType match {
        case IndexType.RTREE => new STRtree()
        case IndexType.QUADTREE => new Quadtree()
      }

		val geometryFactory = new GeometryFactory()
		rawSpatialCollection.foreach {

			case castedSpatialObject: Envelope =>
				val item = geometryFactory.toGeometry(castedSpatialObject)
				if (castedSpatialObject.getUserData != null) {
					item.setUserData(castedSpatialObject.getUserData)
				}
				rt.insert(castedSpatialObject, item);

			case castedSpatialObject: Geometry =>
				rt.insert(castedSpatialObject.getEnvelopeInternal, castedSpatialObject);

			case _ =>
				throw new Exception(
					"[AbstractSpatialCollection][buildIndex] Unsupported spatial index method.");
		}

		rt.query(new Envelope(0.0, 0.0, 0.0, 0.0))
		this.index = rt
		this.rawSpatialCollection = null

	}

  /**
    * Boundary.
    *
    * @return the envelope
    */
  def boundary(): Envelope = {

    val minXEnvelope = rawSpatialCollection.min(new XMinComparator())
    val minYEnvelope = rawSpatialCollection.min(new YMinComparator())
    val maxXEnvelope = rawSpatialCollection.max(new XMaxComparator())
    val maxYEnvelope = rawSpatialCollection.max(new YMaxComparator())

    val pos = (minXEnvelope, minYEnvelope, maxXEnvelope, maxYEnvelope) match {

      case (minX: Geometry, minY: Geometry, maxX: Geometry, maxY: Geometry) =>
        (minX.getEnvelopeInternal.getMinX,
          minY.getEnvelopeInternal.getMinY,
          maxX.getEnvelopeInternal.getMaxX,
          maxY.getEnvelopeInternal.getMaxY)

      case (minX: Circle, minY: Circle, maxX: Circle, maxY: Circle) =>
        (minX.getMBR.getMinX, minY.getMBR.getMinY, maxX.getMBR.getMaxX, maxY.getMBR.getMaxY)

      case (minX: Envelope, minY: Envelope, maxX: Envelope, maxY: Envelope) =>
        (minX.getMinX, minY.getMinY, maxX.getMaxX, maxY.getMaxY)

    }

    _boundary = new Array[Double](4)
    _boundary(0) = pos._1
    _boundary(1) = pos._2
    _boundary(2) = pos._3
    _boundary(3) = pos._4

    boundaryEnvelope =  new Envelope(_boundary(0),_boundary(2),_boundary(1),_boundary(3))
    boundaryEnvelope
  }


}
