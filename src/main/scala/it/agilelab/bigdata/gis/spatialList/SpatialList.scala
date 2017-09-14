/**
 * FILE: SpatialRDD.java
 * PATH: org.datasyslab.geospark.spatialRDD.SpatialRDD.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab
 * All rights reserved.
 */
package it.agilelab.bigdata.gis.spatialList

import java.io.Serializable

import com.vividsolutions.jts.geom.{Envelope, Geometry, GeometryFactory}
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.quadtree.Quadtree
import com.vividsolutions.jts.index.strtree.STRtree
import it.agilelab.bigdata.gis.enums.IndexType
import it.agilelab.bigdata.gis.geometryObjects.Circle
import it.agilelab.bigdata.gis.utils.{XMaxComparatorS, XMinComparatorS, YMaxComparatorS, YMinComparatorS}


// TODO: Auto-generated Javadoc

/**
 * The Class SpatialList.
 */
abstract class SpatialList extends Serializable{
	
	/** The Constant logger. */
		//val logger = Logger.getLogger(this.getClass)
    
    /** The total number of records. */
    var totalNumberOfRecords = -1
    
    /** The boundary. */
    private var _boundary = new Array[Double](4)
    
    /** The boundary envelope. */
    private var boundaryEnvelope: Envelope  = null
    

    /** The indexed Collection. */
    var indexedCollection: Map[Integer, Object] = null
    
    /** The indexed raw RDD. */
    var index: SpatialIndex = null

    /** The raw spatial RDD. */
    var rawSpatialCollection: List[Object] = null

	/** The grids. */
    var grids: List[Envelope] = null
    

	
	/**
	 * Count without duplicates.
	 *
	 * @return the long
	 */
	def countWithoutDuplicates(): Long =
	{
		rawSpatialCollection.toSet.size
	}
	

	/**
	 * Builds the index.
	 *
	 * @param indexType the index type
	 * @throws Exception the exception
	 */
	def buildIndex(indexType: IndexType): Unit = {

		val rt = if (indexType == IndexType.RTREE) new STRtree() else new Quadtree()
		val geometryFactory = new GeometryFactory();
		this.rawSpatialCollection.foreach(spatialObject => {

			if (spatialObject.isInstanceOf[Envelope]) {
				val castedSpatialObject = spatialObject.asInstanceOf[Envelope];
				val item = geometryFactory.toGeometry(castedSpatialObject);
				if (castedSpatialObject.getUserData() != null) {
					item.setUserData(castedSpatialObject.getUserData());
				}
				rt.insert(castedSpatialObject, item);
			} else if (spatialObject.isInstanceOf[Geometry]) {
				val castedSpatialObject = spatialObject.asInstanceOf[Geometry];
				rt.insert(castedSpatialObject.getEnvelopeInternal(), castedSpatialObject);
			}
			else {
				throw new Exception("[AbstractSpatialCollection][buildIndex] Unsupported spatial index method.");
			}
		})

		rt.query(new Envelope(0.0, 0.0, 0.0, 0.0))
		this.index = rt
		this.rawSpatialCollection = null

	}
	
    /**
     * Boundary.
     *
     * @return the envelope
     */
    def boundary(): Envelope =  {

		try
					{
						val minXEnvelope = this.rawSpatialCollection.min(new XMinComparatorS());
						val minYEnvelope = this.rawSpatialCollection.min(new YMinComparatorS());
						val maxXEnvelope = this.rawSpatialCollection.max(new XMaxComparatorS());
						val maxYEnvelope = this.rawSpatialCollection.max(new YMaxComparatorS());
						this._boundary = new Array[Double](0)
						this._boundary = this._boundary.:+(minXEnvelope.asInstanceOf[Geometry].getEnvelopeInternal().getMinX())
						this._boundary = this._boundary.:+(minYEnvelope.asInstanceOf[Geometry].getEnvelopeInternal().getMinY())
						this._boundary = this._boundary.:+(maxXEnvelope.asInstanceOf[Geometry].getEnvelopeInternal().getMaxX())
						this._boundary = this._boundary.:+(maxYEnvelope.asInstanceOf[Geometry].getEnvelopeInternal().getMaxY())
					}
					catch
					{
						case castError: ClassCastException => {
							if (castError.getMessage().contains("Circle")) {
								val minXEnvelope = this.rawSpatialCollection.min(new XMinComparatorS());
								val minYEnvelope = this.rawSpatialCollection.min(new YMinComparatorS());
								val maxXEnvelope = this.rawSpatialCollection.max(new XMaxComparatorS());
								val maxYEnvelope = this.rawSpatialCollection.max(new YMaxComparatorS());
								this._boundary = new Array[Double](0)
								this._boundary = this._boundary.:+(minXEnvelope.asInstanceOf[Circle].getMBR().getMinX());
								this._boundary = this._boundary.:+(minYEnvelope.asInstanceOf[Circle].getMBR().getMinY());
								this._boundary = this._boundary.:+(maxXEnvelope.asInstanceOf[Circle].getMBR().getMaxX());
								this._boundary = this._boundary.:+(maxYEnvelope.asInstanceOf[Circle].getMBR().getMaxY());
							}
							else if (castError.getMessage().contains("Envelope")) {
								val minXEnvelope = this.rawSpatialCollection.min(new XMinComparatorS());
								val minYEnvelope = this.rawSpatialCollection.min(new YMinComparatorS());
								val maxXEnvelope = this.rawSpatialCollection.max(new XMaxComparatorS());
								val maxYEnvelope = this.rawSpatialCollection.max(new YMaxComparatorS());

								this._boundary = new Array[Double](0)
								this._boundary = this._boundary.:+(minXEnvelope.asInstanceOf[Envelope].getMinX());
								this._boundary = this._boundary.:+(minYEnvelope.asInstanceOf[Envelope].getMinY());
								this._boundary = this._boundary.:+(maxXEnvelope.asInstanceOf[Envelope].getMaxX());
								this._boundary = this._boundary.:+(maxYEnvelope.asInstanceOf[Envelope].getMaxY());
							}
						}
					}
					this.boundaryEnvelope =  new Envelope(_boundary(0),_boundary(2),_boundary(1),_boundary(3));
					return this.boundaryEnvelope;
    }


}
