package model

import com.typesafe.scalalogging.LazyLogging
import javafx.scene.image.Image
import javafx.scene.text.{Font, FontWeight, Text, TextFlow}
import model.DecoratedText.DecoratedLine
import model.RunConfig.Keys._

object TextRenderer extends LazyLogging {
  private def createDecoratedText(decoratedText: DecoratedText, fontSize: Int)(implicit rc: RunConfig): Text = {
    val text = new Text(decoratedText.rawText)
    text.setFont(Font.font(rc.getString(FONT_FAMILY), FontWeight.BOLD, fontSize))
    // TODO: Add decorations
    text
  }

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

  def convertEnglishLineToImage(englishLine: String, fontSize: Int)(implicit rc: RunConfig): Image = {
    // First, we see if any line is big enough to warrant using two lines
    val availableScreenWidth = rc.getInt(RESOLUTION_WIDTH) - 2 * rc.getInt(HORIZONTAL_PADDING)

    if(!GraphicsRenderer.canFitOnOneLine(englishLine, fontSize, availableScreenWidth, rc.getString(FONT_FAMILY)) || englishLine.contains("\\")) {
      val (topLine, botLine) = TextProcessor.cleaveSentence(englishLine)
      GraphicsRenderer.renderEnglishTextLine(topLine, fontSize, rc.getString(FONT_FAMILY), rc.getInt(LINE_SPACING), rc.getBool(TRANSPARENT_STROKED), Some(botLine))
    } else {
      GraphicsRenderer.renderEnglishTextLine(englishLine, fontSize, rc.getString(FONT_FAMILY), rc.getInt(LINE_SPACING), rc.getBool(TRANSPARENT_STROKED))
    }
  }
}
