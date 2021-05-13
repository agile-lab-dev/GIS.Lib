package it.agilelab.gis.core.utils

import java.io.File
import scala.util.{ Failure, Success, Try }

trait ValidationUtils extends Logger {

  def tryOrLog[T](v: => T): Try[T] =
    Try(v) recoverWith { case exception: Exception =>
      logger.error(exception.getMessage)
      Failure(exception)
    }

  def checkIsDirectory(name: String): Try[String] = {
    val dir: File = new File(name)
    if (!dir.isDirectory) {
      Failure(new IllegalArgumentException("Expected a directory as graph's location"))
    } else {
      Success(name)
    }
  }
}
