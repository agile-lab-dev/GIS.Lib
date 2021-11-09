package it.agilelab.gis.core.utils

import com.vividsolutions.jts.geom.{ Coordinate, Geometry, Point }
import com.vividsolutions.jts.io.{ ParseException, WKTReader }
import it.agilelab.gis.core.model.geometry.GeometryFactoryEnriched
import java.io.{ IOException, Reader, StreamTokenizer, StringReader }

import scala.util.{ Failure, Success, Try }

/** Extension of vividsolution WKTReader
  * read function can also parse a circle WKT
  */
class WKTReaderRich extends WKTReader {
  private val Empty = "EMPTY"
  private val Comma = ","
  private val L_Paren = "("
  private val R_Paren = ")"
  private val CIRCLE = "CIRCLE"
  private val Circle = "Circle"
  private val LC_circle = "circle"

  private val geometryFactory = new GeometryFactoryEnriched()

  /** Override of read function, if wkt is a circle will call readCircle specific function
    * otherwise will call super class read function
    *
    * @param wellKnownText WKT string to be parsed
    * @return Geometry
    */
  override def read(wellKnownText: String): Geometry = {
    val reader = new StringReader(wellKnownText)
    val tokenizer = new StreamTokenizer(reader)
    try getNextWord(tokenizer) match {
      case Circle | CIRCLE | LC_circle =>
        readCircle(tokenizer) match {
          case Success(geom)      => geom
          case Failure(exception) => throw new ParseException("Circle wrongly formatted")
        }
      case _ =>
        val reader = new StringReader(wellKnownText)
        super.read(reader)
    }
  }

  /** Read circle will parse WKT string and create a Circle
    *
    * @param tokenizer needed to parse the string
    * @return a Circle
    */
  def readCircle(tokenizer: StreamTokenizer): Try[Geometry] =
    readPointText(tokenizer) match {
      case Some(point) =>
        Try {
          getNextEmptyOrCloser(tokenizer) // to remove ')'
          getNextEmptyOrCloser(tokenizer) // to remove ','
          geometryFactory.createCircle(point, getNextNumber(tokenizer))
        } match {
          case Success(value) => Success(value)
          case Failure(ex)    => Failure(new ParseException("Circle wrongly formatted: cannot create circle"))
        }
      case None => Failure(new ParseException("Circle wrongly formatted: cannot read center point"))
    }

  /** @param tokenizer
    * @return String with the parsed content
    */
  private def getNextWord(tokenizer: StreamTokenizer): String = {
    val token = tokenizer.nextToken
    token match {
      case StreamTokenizer.TT_WORD =>
        val word = tokenizer.sval
        if (word.equalsIgnoreCase(Empty)) return Empty
        return word
      case '(' =>
        return L_Paren
      case ')' =>
        return R_Paren
      case ',' =>
        return Comma
    }
    throw new ParseException("Circle wrongly formatted: cannot read next word")
    null
  }

  /** @param tokenizer
    * @return create a Point starting from parsed wkt string
    */
  private def readPointText(tokenizer: StreamTokenizer): Option[Point] = {
    val nextToken = getNextEmptyOrOpener(tokenizer)
    if (nextToken == Empty) None
    else {
      getNextEmptyOrOpener(tokenizer) match {
        case Empty => None
        case L_Paren =>
          Some(geometryFactory.createPoint(new Coordinate(getNextNumber(tokenizer), getNextNumber(tokenizer))))
        case _ => throw new ParseException("Circle coordinates wrongly formatted")
      }
    }
  }

  /** @param tokenizer
    * @return string if it is an empty string or '('
    */
  private def getNextEmptyOrOpener(tokenizer: StreamTokenizer): String = {
    val nextWord = getNextWord(tokenizer)
    if (nextWord == Empty || nextWord == L_Paren) return nextWord
    throw new ParseException(Empty + " or " + L_Paren + " missing")
    null
  }

  /** @param tokenizer
    * @return string if it is an empty string or ')'
    */
  private def getNextEmptyOrCloser(tokenizer: StreamTokenizer): String = {
    val nextWord = getNextWord(tokenizer)
    if (nextWord == Empty || nextWord == R_Paren || nextWord == Comma) return nextWord
    throw new ParseException(Empty + " or " + R_Paren + " missing")
    null
  }

  /** @param tokenizer
    * @return Double if next chars in parsed string are numbers
    */
  private def getNextNumber(tokenizer: StreamTokenizer): Double = {
    val number = tokenizer.nextToken
    number match {
      case StreamTokenizer.TT_NUMBER => tokenizer.nval
      case _                         => throw new ParseException("Invalid symbol: " + tokenizer.sval)
    }
  }
}
