package it.agilelab.bigdata.gis.loader

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory
import it.agilelab.bigdata.gis.enums.IndexType
import it.agilelab.bigdata.gis.models.OSMStreet
import it.agilelab.bigdata.gis.spatialList.GeometryList

import scala.io.Source
import scala.util.Try

/**
  * Created by paolo on 25/01/2017.
  */
trait Loader[T <: Geometry] {

  def loadFile(source: String): Iterator[(Array[AnyRef],Geometry)]

  def loadIndex(sources: String*): GeometryList[T]

}

