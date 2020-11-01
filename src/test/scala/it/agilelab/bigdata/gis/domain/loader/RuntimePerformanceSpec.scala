package it.agilelab.bigdata.gis.domain.loader

import org.scalatest.FlatSpec

class RuntimePerformanceSpec extends FlatSpec {

  def printMemory(r: Runtime): Unit = {
    val mb: Int = 1024 * 1024

    println(s"[MEMORY] Total memory: ${r.totalMemory()/mb} MB")
    println(s"[MEMORY] Used memory: ${(r.totalMemory() - r.freeMemory())/mb} MB")
    println(s"[MEMORY] Free memory: ${r.freeMemory()/mb} MB")
    println(s"[MEMORY] Max memory: ${r.maxMemory()/mb} MB")
  }

  def measuresMemory[R](block: => R): R = {
    val runtime: Runtime = Runtime.getRuntime
    println("--- BEFORE ---")
    printMemory(runtime)
    val res = block
    println("--- AFTER ---")
    printMemory(runtime)
    res
  }

  "Memory consumption" should "be ok" in {
    measuresMemory {
      val t: Long = 1000000000
      val r = 5500000

      val s = t - r
      s
    }
  }

}

