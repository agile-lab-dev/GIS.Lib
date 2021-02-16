package it.agilelab.bigdata.gis.core.utils

import it.agilelab.bigdata.gis.domain.configuration.GraphHopperConfiguration.logger

import java.io.File
import scala.util.{Failure, Success, Try}

trait ValidationUtils {

  def tryOrLog[T](v: => T): Try [T] = {
    Try(v) recoverWith {
      case exception: Exception => logger.error(exception.getMessage)
        Failure(exception)
    }
  }

  def checkIsDirectory(name: String): Try[String] = {
    val dir: File = new File(name)
    if (!dir.isDirectory) {
      Failure(new IllegalArgumentException("Expected a directory as graph's location"))
    }
    else{
      Success(name)
    }
  }
}


