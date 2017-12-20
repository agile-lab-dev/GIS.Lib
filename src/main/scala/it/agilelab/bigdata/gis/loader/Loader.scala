package it.agilelab.bigdata.gis.loader

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory
import it.agilelab.bigdata.gis.enums.IndexType
import it.agilelab.bigdata.gis.models.{HereMapsStreet, OSMStreet, OSMStreetType}
import it.agilelab.bigdata.gis.spatialList.GeometryList

import scala.io.Source
import scala.util.Try

/**
  * Created by paolo on 25/01/2017.
  */
trait Loader[T <: Geometry] {

  protected def loadFile(source: String): Iterator[(Array[AnyRef],Geometry)]

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): T

  def buildIndex( streets: Iterator[T] ): GeometryList[T] = {
    val streetL = streets.toList
    println("starting to build index")
    val streetIndex = new GeometryList[T](streetL)
    streetIndex.buildIndex(IndexType.RTREE)
    println("index built")
    streetIndex
  }

  def loadObjects(sources: String*): Iterator[T] = {
    var i = 0

    val lines: Iterator[T] = sources.foldLeft(Seq.empty[T].toIterator)( (acc, source) => acc ++ loadFile(source).map(e => {


      if(i % 10000 == 0){
        println("loaded "+i+" lines")
      }

      val lr: Geometry = e._2
      val fields = e._1

      i += 1
      objectMapping(fields, lr)
    }))

    lines
  }

  def loadIndex(sources: String*): GeometryList[T] = loadIndexWithFilter(sources:_*)()
  def loadIndexWithFilter(sources: String*)(filterFunc: T => Boolean = _ => true): GeometryList[T] = buildIndex(loadObjects(sources:_*).filter(filterFunc))



}

