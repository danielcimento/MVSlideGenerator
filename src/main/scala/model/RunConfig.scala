package model

import java.io._
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class RunConfig(properties: mutable.Map[String, String], propertiesFile: Option[File]) {
  def updateProperty[A](setting: String, newValue: A): A = {
    properties.put(setting, newValue.toString)
    // Save the new property
    propertiesFile match {
      case Some(propFile) =>
        val fos = new OutputStreamWriter(new FileOutputStream(propFile), "UTF-8")
        val props = new Properties()
        properties.foreach({ case (key, value) => props.setProperty(key, value.toString) })
        props.store(fos, null)
        fos.close()
      case None => // no-op
    }
    newValue
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
}

object RunConfig extends LazyLogging {
  object Keys {
    val RESOLUTION_WIDTH = "resolutionWidth"
    val RESOLUTION_HEIGHT = "resolutionHeight"
    val HORIZONTAL_PADDING = "horizontalPadding"
    val FONT_FAMILY = "fontFamily"
    val LINE_SPACING = "lineSpacing"
    val FURIGANA_SPACING = "furiganaSpacing"
    val TRANSPARENT_STROKED = "transparentStroked"
    val WITH_PREVIEW_LINE = "includePreviewLine"
    val MIN_FONT_BEFORE_CLEAVE = "minFontBeforeCleave"
  }

  import Keys._
  private val defaultSettings = Map(
    RESOLUTION_WIDTH -> 1920,
    RESOLUTION_HEIGHT -> 1080,
    HORIZONTAL_PADDING -> 50,
    FONT_FAMILY -> "Meiryo",
    LINE_SPACING -> 50,
    FURIGANA_SPACING -> 15,
    TRANSPARENT_STROKED -> false,
    // TODO: Experiment with this value
    MIN_FONT_BEFORE_CLEAVE -> 70,
    WITH_PREVIEW_LINE -> false
  )

  def getUserConfig: RunConfig = {
    try {
      val props = new Properties()
      val propFile = new File(new File(getUserAppDataFolder), "mv_slide_generator/properties.conf")
      logger.debug(s"Getting run config from: ${propFile.getAbsolutePath}")
      propFile.getParentFile.mkdirs
      if(!propFile.exists()) {
        propFile.createNewFile()
        val fos = new OutputStreamWriter(new FileOutputStream(propFile), "UTF-8")
        defaultSettings.foreach({ case (key, value) => props.setProperty(key, value.toString) })
        props.store(fos, null)
        fos.close()
        new RunConfig(props.asScala, Some(propFile))
      } else {
        val fis = new InputStreamReader(new FileInputStream(propFile), "UTF-8")
        props.load(fis)
        fis.close()
        new RunConfig(props.asScala, Some(propFile))
      }
    } catch {
      case _: FileNotFoundException => new RunConfig(mutable.Map(), None)
    }
  }

  def getUserAppDataFolder: String = {
    sys.env.get("APPDATA") match {
      case Some(dir) => dir
      case _ => sys.props.get("user.home").getOrElse(throw new FileNotFoundException("Couldn't find a home directory for the user"))
    }
  }
}

