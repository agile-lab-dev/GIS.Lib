package it.agilelab.gis.core.utils

import org.locationtech.jts.geom.Coordinate

object Lines {

  def buildLines(line: Array[Coordinate]): Array[Coordinate] =
    if (line.length > 1) {
      line.sliding(2, 1).flatMap(x => buildLine(new Coordinate(x(0).x, x(0).y), new Coordinate(x(1).x, x(1).y))).toArray
    } else {
      line
    }

  private def buildLine(p1: Coordinate, p2: Coordinate, n: Int = 25): Array[Coordinate] = {
    val stepX = (p2.x - p1.x) / n
    val stepY = (p2.y - p1.y) / n

    var result = Array[Coordinate]()
    for (i <- 1 until n)
      result = result :+ new Coordinate(p1.x + stepX * i, p1.y + stepY * i)
    result = result.filter(p =>
      ((p1.x < p.x && p.x < p2.x) || (p2.x < p.x && p.x < p1.x)) &&
      ((p1.y < p.y && p.y < p2.y) || (p2.y < p.y && p.y < p1.y)))
    result = p1 +: result
    result = result :+ p2
    result
  }

}
