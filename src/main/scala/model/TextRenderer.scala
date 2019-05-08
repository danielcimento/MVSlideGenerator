package model

import com.typesafe.scalalogging.LazyLogging
import javafx.scene.image.Image
import model.RunConfig.Keys._

object TextRenderer extends LazyLogging {
  def getLargestFontSize(lines: List[String], considerSplitting: Boolean = false)(implicit rc: RunConfig): Int = {
    if (lines.isEmpty) {
      0
    } else {
      val availableScreenWidth = rc.getInt(RESOLUTION_WIDTH) - 2 * rc.getInt(HORIZONTAL_PADDING)
      val twoLines = considerSplitting && lines.exists(!GraphicsRenderer.canFitOnOneLine(_, rc.getInt(MIN_FONT_BEFORE_CLEAVE), availableScreenWidth, rc.getString(FONT_FAMILY)))

      def getFontSize: String => Int = GraphicsRenderer.getFontSizeForLine(_, availableScreenWidth, rc.getString(FONT_FAMILY), twoLines)
      lines.map(getFontSize).min
    }
  }

//  def convertJapaneseLinesToImages(japaneseLines: List[String], rc: RunConfig): List[Image] = {
//    // We want to know what the largest Japanese font size we can use is, so we go through each line of text
//    // (x._1 => line), and calculate what the font size for that line should be, within our given bounds
//    // Then, we take the minimum font size, so that all our lines have the same font
//    val rawJapaneseLines = japaneseLines.map(TextProcessor.partitionLinesAndReadings).map(_._1)
//    val jpFontSize = getLargestFontSize(rawJapaneseLines, rc)
//
//    logger.debug(s"Decided on Japanese font size $jpFontSize.")
//
//    japaneseLines.map(convertJapaneseLineToImage(_, jpFontSize, rc))
//  }

  def convertJapaneseLineToImage(japaneseLine: String, fontSize: Int, isPreview: Boolean = false)(implicit rc: RunConfig): Image = {
    // First, we divide each Japanese line into a (line, readings) pair,
    // where the readings are the chunks of (Kanji + Furigana) or (Kana + EmptyString)
    // and the line is the original line with the furigana formatting removed (i.e. the whole "base line" of text)
    // Next, we build the images of the Japanese text. To do so, we turn each (Kanji, Reading) pair into a slice of the
    // sentence. We put together all the slices, taking into account overhang
    val effectiveFontSize = if(!isPreview) fontSize else (fontSize * 0.75).toInt
    TextProcessor.partitionLinesAndReadings(japaneseLine) match {
      case (_, textAndFurigana) if textAndFurigana.nonEmpty =>
        val furiganaFragments = textAndFurigana.map(GraphicsRenderer.renderFuriganaFragment(_, effectiveFontSize, isPreview))
        GraphicsRenderer.joinFuriganaFragments(textAndFurigana.zip(furiganaFragments), effectiveFontSize)
      case _ =>
        GraphicsRenderer.renderFuriganaFragment(TextWithReading("", ""), effectiveFontSize, isPreview)
    }
  }


  def convertEnglishLinesToImages(englishLines: List[String])(implicit rc: RunConfig): List[Image] = {
    // First we calculate the English font size to be used. If we know that an English line needs to be broken into
    // two lines (due to spacing restriction), we calculate the largest possible font size when it is evenly divided
    // into two lines. Once we have each one calculated, we take the min, so that all can have the same size.
    val engFontSize: Int = getLargestFontSize(englishLines, considerSplitting = true)

    logger.debug(s"Decided on font size $engFontSize.")

    // Next, we create the English translation images. The way we do so is by cleaving our sentence (if our chosen font
    // size requires it) and rendering the top and bottom lines above each other.
    englishLines.map(convertEnglishLineToImage(_, engFontSize))
  }

  def convertEnglishLineToImage(englishLine: String, fontSize: Int)(implicit rc: RunConfig): Image = {
    // First, we see if any line is big enough to warrant using two lines
    val availableScreenWidth = rc.getInt(RESOLUTION_WIDTH) - 2 * rc.getInt(HORIZONTAL_PADDING)

    if(GraphicsRenderer.canFitOnOneLine(englishLine, fontSize, availableScreenWidth, rc.getString(FONT_FAMILY))) {
      GraphicsRenderer.renderEnglishTextLine(englishLine, fontSize, rc.getString(FONT_FAMILY), rc.getInt(LINE_SPACING), rc.getBool(TRANSPARENT_STROKED))
    } else {
      val (topLine, botLine) = TextProcessor.cleaveSentence(englishLine)
      GraphicsRenderer.renderEnglishTextLine(topLine, fontSize, rc.getString(FONT_FAMILY), rc.getInt(LINE_SPACING), rc.getBool(TRANSPARENT_STROKED), Some(botLine))
    }
  }
}
