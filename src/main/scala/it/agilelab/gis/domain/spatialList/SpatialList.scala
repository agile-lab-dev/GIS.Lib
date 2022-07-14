package it.agilelab.gis.domain.spatialList

import com.vividsolutions.jts.geom.{ Coordinate, Envelope, Geometry, GeometryFactory }
import com.vividsolutions.jts.index.SpatialIndex
import com.vividsolutions.jts.index.quadtree.Quadtree
import com.vividsolutions.jts.index.strtree.STRtree
import it.agilelab.gis.core.model.IndexType
import it.agilelab.gis.core.utils.{ XMaxComparator, XMinComparator, YMaxComparator, YMinComparator }

import java.io.Serializable

/** The Class SpatialList.
  */
@SerialVersionUID(1L)
abstract class SpatialList extends Serializable {

  /** The total number of records. */
  var totalNumberOfRecords: Int = -1

  /** The boundary. */
  var _boundary = new Array[Double](4)

  /** The boundary envelope. */
  var boundaryEnvelope: Envelope = _

  /** The indexed Collection. */
  var indexedCollection: Map[Integer, Object] = _

  /** The indexed raw. */
  var index: SpatialIndex = _

  /** The raw spatial. */
  var rawSpatialCollection: List[Geometry] = _

  var rawForBoundary: List[Object] = _

  /** The grids. */
  var grids: List[Envelope] = _

  /** Count without duplicates.
    *
    * @return the long
    */
  def countWithoutDuplicates(): Long =
    rawSpatialCollection.toSet.size.toLong

  /** Builds the index.
    *
    * @param indexType the index type
    * @throws Exception the exception
    */
  def buildIndex(indexType: IndexType): Unit = {

    val rt =
      indexType match {
        case IndexType.RTREE    => new STRtree()
        case IndexType.QUADTREE => new Quadtree()
      }

    rawSpatialCollection.foreach(geometry => rt.insert(geometry.getEnvelopeInternal, geometry))

    rt.query(new Envelope(0.0, 0.0, 0.0, 0.0)) // TODO: to understand
    index = rt
    rawSpatialCollection = null

  }

  /** Boundary.
    *
    * @return the envelope
    */
  def boundary(): Envelope = {

    val minXEnvelope = rawSpatialCollection.min(new XMinComparator())
    val minYEnvelope = rawSpatialCollection.min(new YMinComparator())
    val maxXEnvelope = rawSpatialCollection.max(new XMaxComparator())
    val maxYEnvelope = rawSpatialCollection.max(new YMaxComparator())

    boundaryEnvelope = new Envelope(
      minXEnvelope.getEnvelopeInternal.getMinX,
      minYEnvelope.getEnvelopeInternal.getMinY,
      maxXEnvelope.getEnvelopeInternal.getMaxX,
      maxYEnvelope.getEnvelopeInternal.getMaxY)
    boundaryEnvelope
  }

  def contains(coordinate: Coordinate): Boolean =
    boundaryEnvelope.contains(coordinate)

}
