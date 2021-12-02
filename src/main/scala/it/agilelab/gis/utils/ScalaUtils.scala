package it.agilelab.gis.utils

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
}
