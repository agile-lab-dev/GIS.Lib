package it.agilelab.bigdata.gis.core.loader

import com.vividsolutions.jts.geom._
import it.agilelab.bigdata.gis.core.model.IndexType
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList


/**
  * Created by paolo on 25/01/2017.
  */
trait Loader[T <: Geometry] {

  protected def loadFile(source: String): Iterator[(Array[AnyRef],Geometry)]

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): T

  def buildIndex( objects: Iterator[T] ): GeometryList[T] = {
    val objectL = objects.toList
    println("[GISLib] Starting to build R-Tree")
    val objectIndex= new GeometryList[T](objectL)
    objectIndex.buildIndex(IndexType.RTREE)
    println("[GISLib] R-Tree built")
    objectIndex
  }

  def loadObjects(sources: String*): Iterator[T] = {

    val lines: Iterator[T] = sources.foldLeft(Seq.empty[T].toIterator)( (acc, source) => acc ++ loadFile(source).map(e => {

      val lr: Geometry = e._2
      val fields = e._1

      objectMapping(fields, lr)
    }))

    lines
  }

  def loadIndex(sources: String*): GeometryList[T] = loadIndexWithFilter(sources:_*)()
  def loadIndexWithFilter(sources: String*)(filterFunc: T => Boolean = _ => true): GeometryList[T] = buildIndex(loadObjects(sources:_*).filter(filterFunc))



}

