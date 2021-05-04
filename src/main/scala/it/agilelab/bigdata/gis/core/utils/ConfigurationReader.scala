package it.agilelab.bigdata.gis.core.utils

import com.typesafe.config.Config
import scala.collection.JavaConversions._

trait ConfigurationReader[T] {
  def read(config: Config, key: String): T
}

object ConfigurationReader {
  implicit object StringConfigurationReader extends ConfigurationReader[String] {
    override def read(config: Config, key: String): String = config.getString(key)
  }

  implicit object IntConfigurationReader extends ConfigurationReader[Int] {
    override def read(config: Config, key: String): Int = config.getInt(key)
  }

  implicit object BooleanConfigurationReader extends ConfigurationReader[Boolean] {
    override def read(config: Config, key: String): Boolean = config.getBoolean(key)
  }

  implicit object DoubleConfigurationReader extends ConfigurationReader[Double] {
    override def read(config: Config, key: String): Double = config.getDouble(key)
  }

  implicit object ConfigConfigurationReader extends ConfigurationReader[Config] {
    override def read(config: Config, key: String): Config = config.getConfig(key)
  }

  implicit object StringListConfigurationReader extends ConfigurationReader[List[String]] {
    override def read(config: Config, key: String): List[String] = config.getStringList(key).toList
  }
}
