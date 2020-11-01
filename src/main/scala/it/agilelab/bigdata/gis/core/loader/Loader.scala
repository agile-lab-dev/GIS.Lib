package it.agilelab.bigdata.gis.core.loader

import com.vividsolutions.jts.geom._
import it.agilelab.bigdata.gis.core.model.IndexType
import it.agilelab.bigdata.gis.core.utils.Logger
import it.agilelab.bigdata.gis.domain.spatialList.GeometryList


/**
  * Created by paolo on 25/01/2017.
  */
trait Loader[T <: Geometry] extends Logger{

  def loadIndex(sources: String*): GeometryList[T] = loadIndexWithFilter(sources:_*)()

  def loadIndexWithFilter(sources: String*)(filterFunc: T => Boolean = _ => true): GeometryList[T] = {
    val objects = loadObjects(sources: _*).filter(filterFunc)
    buildIndex(objects)

  }

  def loadObjects(sources: String*): List[T] = {
    val lines: Iterator[T] = sources
      .foldLeft(Seq.empty[T].toIterator)(
        (acc, source) =>
          acc ++ loadFile(source).map(e => {objectMapping(e._1, e._2)})
      )
    lines.toList
  }

  protected def loadFile(source: String): Iterator[(Array[AnyRef],Geometry)]

  protected def objectMapping(fields: Array[AnyRef], line: Geometry): T

  def buildIndex(objects: List[T]): GeometryList[T] = {
    logger.info("Starting to build R-Tree")
    val start = System.currentTimeMillis()
    val objectIndex: GeometryList[T] = new GeometryList[T](objects)
    objectIndex.buildIndex(IndexType.RTREE)
    logger.info(s"R-Tree built in ${System.currentTimeMillis() - start} (ms)")
    objectIndex
  }

}

