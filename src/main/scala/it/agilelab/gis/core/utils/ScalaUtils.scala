package it.agilelab.gis.core.utils

import com.typesafe.config.Config

import scala.collection.JavaConverters.mapAsScalaMapConverter

object ScalaUtils {
  implicit class ConfigMapExtract(conf: Config) {

    /** Gets configs as Map from the path read in input. Map values are returned as generic objects
      * @param path path from which configs are read
      * @return a Map with configs
      */
    def getMap(path: String): Map[String, Any] =
      conf
        .getObject(path)
        .asScala
        .map { case (k, v) => k -> v.unwrapped() }(collection.breakOut)
  }
}
