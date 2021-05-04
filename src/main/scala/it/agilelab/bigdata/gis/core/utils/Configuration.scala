package it.agilelab.bigdata.gis.core.utils

import com.typesafe.config.Config
import scala.util.{ Failure, Success, Try }

trait Configuration {

  def read[T: ConfigurationReader](conf: Config, key: String): Try[T] =
    if (conf.hasPath(key)) {
      Try(implicitly[ConfigurationReader[T]].read(conf, key))
    } else {
      Failure(new IllegalArgumentException(s"Configuration value $key is not set."))
    }

  def readOptional[T: ConfigurationReader](conf: Config, key: String): Try[Option[T]] =
    if (conf.hasPath(key)) {
      Try(Some(implicitly[ConfigurationReader[T]].read(conf, key)))
    } else {
      Success(None)
    }
}
