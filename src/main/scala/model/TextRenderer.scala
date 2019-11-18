package model

import com.typesafe.scalalogging.LazyLogging
import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text._
import model.DecoratedText.DecoratedLine
import model.RunConfig.Keys._

object TextRenderer extends LazyLogging {

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

  def renderEnglishLine(englishLine: String, fontSize: Int)(implicit rc: RunConfig): HeightAwareTextFlow = {
    val lineWithDecorations = TextProcessor.decorateLineOfText(englishLine)
    val availableScreenWidth = rc.getInt(RESOLUTION_WIDTH) - 2 * rc.getInt(HORIZONTAL_PADDING)
    val textFlow = new HeightAwareTextFlow()

    var totalWidth = 0.0
    lineWithDecorations.foreach(fragment => {
      val textSegment = createDecoratedText(fragment, fontSize)
      textFlow.addChild(textSegment)
      totalWidth += textSegment.getLayoutBounds.getWidth
    })

    println(totalWidth)
    textFlow.setLineSpacing(rc.getInt(LINE_SPACING))

    if (totalWidth > availableScreenWidth) {
      // TODO: tidy
      textFlow.setMaxWidth(maxWidthAfterSplittingLine(englishLine, Font.font(rc.getString(FONT_FAMILY), FontWeight.BOLD, fontSize)))
      textFlow.lineCount = 2
    } else {
      textFlow.setMaxWidth(totalWidth)
    }

    textFlow.setTextAlignment(TextAlignment.CENTER)
    textFlow
  }

  // Figure out how long the top line will be
  private def maxWidthAfterSplittingLine(line: String, font: Font): Double = {
    // We continue to take words until the number of characters we've accumulated (taking into account spaces) is over
    // half of the sentence. Since we always want the top longer than the bottom, we move one element from the second half to the first
    val fullSentence = new Text(line)
    fullSentence.setFont(font)

    var runningLength = 0.0
    val (firstHalf, secondHalf) = line.split(' ').span { str =>
      val txt = new Text(str + " ")
      txt.setFont(font)
      runningLength += txt.getLayoutBounds.getWidth
      runningLength < fullSentence.getLayoutBounds.getWidth / 2
    } match {
      case (h, t) => (h ++ t.take(1), t.drop(1))
    }

    val topHalf = new Text(firstHalf.reduce(_ + " " + _))
    val bottomHalf = new Text(secondHalf.reduce(_ + " " + _))
    topHalf.setFont(font)
    bottomHalf.setFont(font)
    Math.max(topHalf.getLayoutBounds.getWidth, bottomHalf.getLayoutBounds.getWidth)
  }

  private def createDecoratedText(decoratedText: DecoratedText, fontSize: Int)(implicit rc: RunConfig): Text = {
    val fontPosture = if (decoratedText.decorations.contains(Italics)) FontPosture.ITALIC else FontPosture.REGULAR

    val text = new Text(decoratedText.rawText)
    text.setFont(Font.font(rc.getString(FONT_FAMILY), FontWeight.BOLD, fontPosture, fontSize))
    text.setFill(Color.WHITE)
    // TODO: Add decorations
    text
  }
}
