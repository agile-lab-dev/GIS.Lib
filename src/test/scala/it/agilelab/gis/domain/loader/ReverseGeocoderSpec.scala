package it.agilelab.gis.domain.loader

import it.agilelab.gis.domain.loader.ReverseGeocoder.Index
import org.scalatest.{ FlatSpec, Matchers }

import scala.reflect.runtime.universe

class ReverseGeocoderSpec extends FlatSpec with Matchers {

  it should "return all indices" in {
    val subclasses: Set[universe.Symbol] = universe.typeOf[Index].typeSymbol.asClass.knownDirectSubclasses

    subclasses should have size ReverseGeocoder.indices.size
  }

}
