package it.agilelab.bigdata.gis.domain.configuration

import com.typesafe.config.Config

case class PathManagerConfiguration()

object PathManagerConfiguration {

  def apply(config: Config): PathManagerConfiguration = {
      PathManagerConfiguration()
  }
}




