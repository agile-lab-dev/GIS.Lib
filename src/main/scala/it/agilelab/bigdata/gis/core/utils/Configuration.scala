package it.agilelab.bigdata.gis.core.utils

import com.typesafe.config.Config
import scala.util.{Failure, Success, Try}


trait Configuration {

  def read[T: ConfigurationReader](conf:Config, key: String): Try[T] = {

    if(conf.hasPath(key)){
      Try(implicitly[ConfigurationReader[T]].read(conf, key))
    }
    else{
      Failure(new IllegalArgumentException(s"Configuration value $key is not set."))
    }
  }

  def readOptional[T](conf:Config, key: String)( implicit mapper: (Config,String) => T): Try[Option[T]] = {

    if(conf.hasPath(key)){
      Try(Some(mapper(conf,key)))
    }
    else{
      Success(None)
    }
  }

}


