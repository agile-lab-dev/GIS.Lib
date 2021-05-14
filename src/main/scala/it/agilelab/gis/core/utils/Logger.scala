package it.agilelab.gis.core.utils

import org.slf4j.LoggerFactory

trait Logger {

  @transient
  protected lazy val logger: org.slf4j.Logger = LoggerFactory.getLogger(getClass.getName)
}
