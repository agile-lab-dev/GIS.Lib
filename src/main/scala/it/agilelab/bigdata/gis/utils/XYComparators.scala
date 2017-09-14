package it.agilelab.bigdata.gis.utils

/**
  * Created by paolo on 25/01/2017.
  */

class XMaxComparatorS extends Ordering[Object] {
  override def compare(x: Object, y: Object): Int = new XMaxComparator().compare(x,y)
}

class XMinComparatorS extends Ordering[Object] {
  override def compare(x: Object, y: Object): Int = new XMinComparator().compare(x,y)
}

class YMaxComparatorS extends Ordering[Object] {
  override def compare(x: Object, y: Object): Int = new YMaxComparator().compare(x,y)
}

class YMinComparatorS extends Ordering[Object] {
  override def compare(x: Object, y: Object): Int = new YMinComparator().compare(x,y)
}
