package it.agilelab.gis.utils

import java.util.concurrent.Callable

/** Generic scala utilities
  *
  * @author Agile Lab s.r.l.
  */
object ScalaUtils {
  def recordDuration[T](f: => T, duration: Long => Unit): T = {
    val start = System.currentTimeMillis()
    val r = f
    duration(System.currentTimeMillis() - start)
    r
  }

  def load[T](f: => T): Callable[T] = new Callable[T] {
    override def call(): T = f
  }
}
