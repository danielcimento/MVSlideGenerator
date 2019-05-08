package model

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, Text, TextAlignment}
import RunConfig.Keys._
import com.typesafe.scalalogging.LazyLogging

import scala.language.postfixOps

object GraphicsRenderer extends GraphicsHelpers {
  private val bigCharacter = "国"

  // determines whether an english sentence can fit entirely on one line or needs to be broken up
  def canFitOnOneLine(line: String, fontSize: Int, width: Int, fontFamily: String): Boolean = {
    getTextDimensions(line, Font.font(fontFamily, FontWeight.BOLD, fontSize))._1 <= width
  }

  private def largestIntSatisfyingPredicate(pred: Int => Boolean): Int = {
    var workingInt = 1

    while(pred(workingInt)) {
      workingInt *= 2
    }

    // The largest possible number is in this range
    val possibleRange = (workingInt / 2 to workingInt).reverse

    possibleRange.find(pred) match {
      case Some(i) => i
      case _ => 0
    }
  }

  // Gets the largest font size within a certain range that can comfortably fit in the specified area
  def getFontSizeForLine(
    line: String,
    availableWidth: Int,
    fontFamily: String,
    twoLines: Boolean = false
  ): Int = {

    if(line.isEmpty) {
      // This usually only becomes relevant in the case of failed splits. It's mostly here to prevent weird interactions, but won't often see much use
      Int.MaxValue
    } else if(twoLines) {
      val (firstLine, secondLine) = TextProcessor.cleaveSentence(line)

      Math.min(
        getFontSizeForLine(firstLine, availableWidth, fontFamily),
        getFontSizeForLine(secondLine, availableWidth, fontFamily)
      )
    } else {
      largestIntSatisfyingPredicate(canFitOnOneLine(line, _, availableWidth, fontFamily))
    }
  }

  def renderFuriganaFragment(
    textWithReading: TextWithReading,
    fontSize: Int,
    isPreview: Boolean,
  )(implicit rc: RunConfig): Image = {
    val (spacing, fontFamily, transparentBolded) = (rc.getInt(FURIGANA_SPACING), rc.getString(FONT_FAMILY), rc.getBool(TRANSPARENT_STROKED))
    val opacity = if(isPreview) 0.5 else 1.0

    val bigFont = Font.font(fontFamily, FontWeight.BOLD, fontSize)
    val smallFont = Font.font(fontFamily, FontWeight.BOLD, fontSize / 2)

    textWithReading match {
      case TextWithReading(baseText, reading) =>

        val (baseWidth, baseHeight) = if(baseText != "") {
          getTextDimensions(baseText, bigFont)
        } else {
          getTextDimensions(bigCharacter, bigFont)
        }
        // We don't know if the reading includes furigana, so we just fake it with the base text.
        val (readingWidth, readingHeight) = if(reading != "") {
          getTextDimensions(reading, smallFont)
        } else {
          getTextDimensions(bigCharacter, smallFont)
        }

        val widthOfCanvas = Math.max(
          baseWidth,
          readingWidth
        )

        logger.debug("The width of %s and %s are %1.2f and %1.2f, so canvas width is %1.2f".format(baseText, reading, baseWidth, readingWidth, widthOfCanvas))

        // TODO: Work out weird constant multiplication? Probably has something to do with DPI, but I have no idea why (or glyph height to width ratio)
        val heightScalingFactor = getFontHeightToWidthRatio(bigFont)
        val scaledFuriganaHeight: Double =  readingHeight * heightScalingFactor
        val scaledBaseHeight = baseHeight * heightScalingFactor

        val heightOfCanvas = scaledFuriganaHeight + scaledBaseHeight + spacing

        logger.debug("The height of the fragment (%s) is %1.2f, since the reading height, text height, and spacing are %1.2f, %1.2f, %d".format(baseText, heightOfCanvas, readingHeight, baseHeight, spacing))

        implicit val canvasForText: Canvas = new Canvas(widthOfCanvas, heightOfCanvas)

        // The Kanji extend around 5 pixels from the base line of the font
        val baseYPosition = scaledFuriganaHeight + spacing + scaledBaseHeight - 6
        writeTextToCanvas(baseText, canvasForText.getWidth / 2, baseYPosition, transparentBolded)(canvasForText, bigFont)

        val readingYPosition = scaledFuriganaHeight
        writeTextToCanvas(reading, canvasForText.getWidth / 2, readingYPosition, transparentBolded)(canvasForText, smallFont)

        takePictureOfCanvas(canvasForText, opacity)
    }
  }

  def joinFuriganaFragments(listOfFragments: List[(TextWithReading, Image)], fontSize: Int)(implicit rc: RunConfig): Image = {
    val font = Font.font(rc.getString(FONT_FAMILY), FontWeight.BOLD, fontSize)
//    val smallFont = Font.font(fontFamily, FontWeight.BOLD, fontSize / 2)

    listOfFragments match {
      // Special case on lines without Furigana
      case List(oneItem) => oneItem._2
      case List() =>
        logger.error("It appears that your lines were too long to fit on one line, but another one of your lines didn't have any spaces to split along")
        throw new MatchError()
      case first +: middle :+ last =>
        (first, last) match {
          case ((firstText, firstImg), (lastText, lastImg)) =>
            val firstTextWidth = getTextWidth(firstText.baseText, font)
            val firstFuriganaOverhang = (firstImg.getWidth - firstTextWidth) / 2

            // We have to account for the image width of the ends in case the furigana overhangs
            val widthOfEnds = firstImg.getWidth + lastImg.getWidth
            logger.debug("%s and %s have a width of %1.2f and %1.2f".format(firstText.baseText, lastText.baseText, firstImg.getWidth, lastImg.getWidth))
            val widthOfMiddleText = middle.map {
              case (txt, _) => getTextWidth(txt.baseText, font)
            } sum
            val canvasWidth: Double = widthOfEnds + widthOfMiddleText
            // get the tallest image in the list (these should be of same size, though)
            val canvasHeight: Double = listOfFragments.map(_._2.getHeight).max

            implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)
            val gc = canvas.getGraphicsContext2D


            gc.drawImage(firstImg, 0.0, 0.0)
            listOfFragments.tail.foldLeft(firstFuriganaOverhang + firstTextWidth) {
              case (xPosition, (textWithReading, image)) =>
                val baseWidth = getTextWidth(textWithReading.baseText, font)
                val overhang = (image.getWidth - baseWidth) / 2
                gc.drawImage(image, xPosition - overhang, 0.0)
                xPosition + baseWidth
            }

//            // The starting x position for the middle element's base text to align on requires a calculation depending on whether the base or furigana was larger
//            var startingXForMiddle: Double = if(firstTextWidth > getTextWidth(firstText.reading, smallFont)) {
//              firstTextWidth
//            } else {
//              firstTextWidth + firstFuriganaOverhang
//            }
//
//            (middle :+ last).foreach {
//              case (txt, img) =>
//                val strWidth = getTextWidth(txt.baseText, font)
//                val overhang = (img.getWidth - strWidth) / 2
//                gc.drawImage(img, startingXForMiddle - overhang, 0)
//                startingXForMiddle += strWidth
//            }

            takePictureOfCanvas
        }
    }
  }

  // Warning: Assumes all images have the same height (maybe will fix later)
  def stackImagesWithSpacing(images: List[Image], spacing: Int): Image = {
    val (canvasWidth, canvasHeight) = (images.map(_.getWidth).max, images.map(_.getHeight).sum + (spacing * (images.length - 1)))
    implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)
    val gc = canvas.getGraphicsContext2D
    images.foldLeft(0.0) { (accHeight, image) =>
        val imageXPos = (canvasWidth - image.getWidth) / 2
        val imageYPos = accHeight
        gc.drawImage(image, imageXPos, imageYPos)
        accHeight + image.getHeight + spacing
    }
    takePictureOfCanvas
  }

  def renderEnglishTextLine(topLine: String,  fontSize: Int, fontFamily: String, spacing: Int, transparentBolded: Boolean, bottomLine: Option[String] = None): Image = {
    implicit val font: Font = Font.font(fontFamily, FontWeight.BOLD, fontSize)
    bottomLine match {
      case Some(botLine) =>
        val (topWidth, topHeight) = getTextDimensions(topLine, font)
        val (botWidth, botHeight) = getTextDimensions(botLine, font)

        val canvasHeight = topHeight + botHeight + spacing
        val canvasWidth = Math.max(topWidth, botWidth)
        implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)

        writeTextToCanvas(topLine, canvasWidth / 2, topHeight * .85, transparentBolded)
        writeTextToCanvas(botLine, canvasWidth / 2, canvasHeight - (botHeight * .42), transparentBolded)
        takePictureOfCanvas
      case None =>
        val (canvasWidth, canvasHeight) = getTextDimensions(topLine, font)
        implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)

        writeTextToCanvas(topLine, canvasWidth / 2, canvasHeight * .75, transparentBolded)
        takePictureOfCanvas
    }
  }

  def paintText(topText: Image, bottomText: Image, previewText: Option[Image], rc: RunConfig): Image = {
    // Unpack run config
    val (xDimension, yDimension) = (rc.getInt(RESOLUTION_WIDTH), rc.getInt(RESOLUTION_HEIGHT))

    implicit val canvas: Canvas = new Canvas(xDimension, yDimension)
    val gc = canvas.getGraphicsContext2D

    if(rc.getBool(TRANSPARENT_STROKED)) {
      gc.setFill(Color.TRANSPARENT)
    } else {
      gc.setFill(Color.BLACK)
    }
    gc.fillRect(0, 0, xDimension, yDimension)

    val thirdOfHeight = yDimension / 3

    val topXPosition = (xDimension - topText.getWidth) / 2
    val topYPosition = (thirdOfHeight - topText.getHeight) / 2
    gc.drawImage(topText, topXPosition, topYPosition)

    val bottomImage: Image = previewText match {
      case None => bottomText
      case Some(img) => stackImagesWithSpacing(List(bottomText, img), rc.getInt(FURIGANA_SPACING))
    }

    val bottomXPosition = (xDimension - bottomImage.getWidth) / 2
    val bottomYPosition = yDimension - bottomImage.getHeight - rc.getInt(LINE_SPACING)
    gc.drawImage(bottomImage, bottomXPosition, bottomYPosition)

    takePictureOfCanvas
  }

  def convertLinesToImage(englishLine: String, japaneseLineAndPreview: (String, String), engFontSize: Int, jpFontSize: Int)(implicit rc: RunConfig): Image = {
    val (japaneseImage, previewImage) = japaneseLineAndPreview match {
      case (line, preview) if rc.getBool(WITH_PREVIEW_LINE) =>
        val f: (String, Boolean) => Image = TextRenderer.convertJapaneseLineToImage(_, jpFontSize, _)
        (f(line, false), Some(f(preview, true)))
      case (line, _) => (TextRenderer.convertJapaneseLineToImage(line, jpFontSize), None)
    }
    val englishImage = TextRenderer.convertEnglishLineToImage(englishLine, engFontSize)
    paintText(englishImage, japaneseImage, previewImage, rc)
  }

  def createAllImages(japaneseLines: List[String], englishLines: List[String], jpFontSize: Int, engFontSize: Int)(implicit rc: RunConfig): List[Image] = {
    // TODO: Fix weird nesting of tuples
    val linePairs: List[(String, (String, String))] = englishLines.zipAll(japaneseLines.zipAll(japaneseLines.tail, "", ""), "", ("", ""))
    linePairs.map { case (eL, jL) => convertLinesToImage(eL, jL, engFontSize, jpFontSize) }
  }


}

trait GraphicsHelpers extends LazyLogging {
  def getFontHeightToWidthRatio(font: Font): Double = {
    val (w, h) = getTextDimensions("世", font)
    w / h
  }

  def getTextWidth(text: String, font: Font): Double = getTextDimensions(text, font)._1
  def getTextHeight(text: String, font: Font): Double = getTextDimensions(text, font)._2

  def getTextDimensions(text: String, font: Font): (Double, Double) = {
    val textObj = new Text(text)
    textObj.setFont(font)
    val bounds = textObj.getLayoutBounds
    logger.debug("String (%s) at size %1.2f takes up (%1.2f, %1.2f)".format(text, font.getSize, bounds.getWidth, bounds.getHeight))
    (bounds.getWidth, bounds.getHeight)
  }

  def takePictureOfCanvas(implicit canvas: Canvas, opacity: Double = 1.0, fill: Color = Color.TRANSPARENT): Image = {
    canvas.setOpacity(opacity)
    val snapshotParameters = new SnapshotParameters
    snapshotParameters.setFill(fill)
    canvas.snapshot(snapshotParameters, null)
  }

  def writeTextToCanvas(
    text: String, xPos: Double, yPos: Double, transparentBolded: Boolean,
    textAlign: TextAlignment = TextAlignment.CENTER, color: Color = Color.WHITE
  )(implicit canvas: Canvas, font: Font): Unit = {
    val gc = canvas.getGraphicsContext2D

    gc.setFont(font)
    gc.setTextAlign(textAlign)
    gc.setFill(color)
    if(transparentBolded) {
      gc.setStroke(Color.BLACK)
      gc.setLineWidth(2.0)
    }

    gc.fillText(text, xPos, yPos)
    if(transparentBolded) {
      gc.strokeText(text, xPos, yPos)
    }
  }
}