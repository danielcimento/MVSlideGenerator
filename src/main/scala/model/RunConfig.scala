package model

import java.io.{File, FileInputStream, FileNotFoundException, InputStreamReader}
import java.util.Properties

import fastparse.NoWhitespace._
import fastparse._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

case class RunConfig(properties: mutable.Map[String, String]) {
  def updateProperty[A](setting: String, value: A): A = {
    properties.put(setting, value.toString)
    // TODO: Save new property
    value
  }

  def getInt(setting: String): Int = {
    properties.get(setting).flatMap(i => Try(i.toInt).toOption) match {
      case Some(i) => i
      case _ => RunConfig.defaultSettings.get(setting) match {
        case Some(i: Int) => updateProperty(setting, i)
        case _ => throw new IllegalArgumentException(s"$setting should not be handled as an int!")
      }
    }
  }

  def getString(setting: String): String = {
    properties.get(setting) match {
      case Some(s) => s
      case _ => RunConfig.defaultSettings.get(setting) match {
        case Some(s: String) => updateProperty(setting, s)
        case _ => throw new IllegalArgumentException(s"$setting should not be handled as a string!")
      }
    }
  }

  def getBool(setting: String): Boolean = {
    properties.get(setting).flatMap(b => Try(b.toBoolean).toOption) match {
      case Some(b) => b
      case _ => RunConfig.defaultSettings.get(setting) match {
        case Some(b: Boolean) => updateProperty(setting, b)
        case _ => throw new IllegalArgumentException(s"$setting should not be handled as a boolean!")
      }
    }
  }
//
//  def getIntTuple(setting: String): (Int, Int) = {
//    def intTupleParse(s: String): Option[(Int, Int)] = {
//      def number[_ : P]: P[Int] = P(CharIn("0-9").rep(1).!.map(_.toInt))
//      def tuple[_: P]: P[(Int, Int)] = P("(" ~ number ~ "," ~ number ~ ")")
//      parse(s, tuple(_)) match {
//        case Parsed.Success(x, _) => Some(x)
//        case _ => None
//      }
//    }
//
//    properties.get(setting).flatMap(intTupleParse) match {
//      case Some(it) => it
//      case _ => RunConfig.defaultSettings.get(setting) match {
//        case Some((a: Int, b: Int)) => updateProperty(setting, (a, b))
//        case _ => throw new IllegalArgumentException(s"$setting should not be handled as an int tuple!")
//      }
//    }
//  }
}

object RunConfig {
  object Keys {
    val PPI = "ppi"
    val RESOLUTION_WIDTH = "resolutionWidth"
    val RESOLUTION_HEIGHT = "resolutionHeight"
    val HORIZONTAL_PADDING = "horizontalPadding"
    val FONT_FAMILY = "fontFamily"
    val LINE_SPACING = "lineSpacing"
    val FURIGANA_SPACING = "furiganaSpacing"
    val TRANSPARENT_STROKED = "transparentStroked"
    val MIN_FONT_BEFORE_CLEAVE = "minFontBeforeCleave"
  }

  import Keys._
  private val defaultSettings = Map(
    PPI -> 72,
    RESOLUTION_WIDTH -> 1920,
    RESOLUTION_HEIGHT -> 1080,
    HORIZONTAL_PADDING -> 50,
    FONT_FAMILY -> "Meiryo",
    LINE_SPACING -> 50,
    FURIGANA_SPACING -> 15,
    TRANSPARENT_STROKED -> false,
    // TODO: Experiment with this value
    MIN_FONT_BEFORE_CLEAVE -> 70
  )

  def getUserConfig(): RunConfig = {
    // TODO: Find an adequate location to hide this file
    try {
      val propFile = new File("properties")
      // TODO: Save a new default config if the file wasn't found
      val props = new Properties()
      val fis = new InputStreamReader(new FileInputStream(propFile), "UTF-8")
      props.load(fis)
      fis.close()
      RunConfig(props.asScala)
    } catch {
      case io: FileNotFoundException => RunConfig(mutable.Map())
    }
  }
}

