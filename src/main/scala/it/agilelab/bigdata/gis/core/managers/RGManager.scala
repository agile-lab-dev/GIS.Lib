package it.agilelab.bigdata.gis.core.managers
import com.vividsolutions.jts.geom.Geometry
import it.agilelab.bigdata.gis.core.model.output.OutputModel
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList

/**
  * @author andreaL
  */


/**
  * Template for reverse geocoding managers
  *
  * @tparam T1 Member of GeometryList
  * @tparam T2 Output type
  */
trait RGManager[T1 <: Geometry, T2 <: OutputModel] {

  /**
    * Geometry list with all points and index
    */
  var geometryList: GeometryList[T1]

  /**
    * Loads geo data into list
    * @param string all required paths, filters, prefixes
    */
  def init(string: String*): Unit

  /**
    * Check if @geometryList initialized
    *
    * @return true if initialized
    */
  def checkGeoList: Boolean = geometryList != null

  /**
    * Reverse geocode input point and filters it
    *
    * @param lat                point's latitude
    * @param long               point's longitude
    * @param filterEmptyStreets filter mode
    * @return Option of output type
    */
  def reverseGeocode(lat: Double, long: Double, filterEmptyStreets: Boolean = false, maxDistance: Double = 200, distanceOp: String = "GraphHopper"): Option[T2]

}

/*
  --------------------
  RGManager extensions
  --------------------
 */
trait RGManager2[T1 <: Geometry, T2 <: Geometry, T3 <: OutputModel] extends RGManager[T1, T3] {

  var geometryList2: GeometryList[T2]

  def checkGeoList2: Boolean = geometryList2 != null

  def checkAllLists: Boolean = checkGeoList && checkGeoList2

}

trait RGManager3[T1 <: Geometry, T2 <: Geometry, T3 <: Geometry, T4 <: OutputModel] extends RGManager2[T1, T2, T4] {

  var geometryList3: GeometryList[T3]

  def checkGeoList3: Boolean = geometryList3 != null

  override def checkAllLists: Boolean = super.checkAllLists && checkGeoList3

}