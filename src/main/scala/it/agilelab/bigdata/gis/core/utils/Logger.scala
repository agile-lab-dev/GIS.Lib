package it.agilelab.bigdata.gis.core.utils
import org.apache.log4j

trait Logger {
  val logger: log4j.Logger = log4j.Logger.getLogger(getClass)
}
