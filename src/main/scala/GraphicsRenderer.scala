package main.scala

import java.util.UUID
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.image.Image
import javafx.scene.paint.{Color, Paint}
import javafx.scene.text.{Font, FontWeight, Text, TextAlignment}

import main.scala.GraphicsRenderer.logger

import scala.language.postfixOps

object GraphicsRenderer extends GraphicsHelpers {
  private val debug = GlobalContext.conf.getBoolean("debug")

  // determines whether an english sentence can fit entirely on one line or needs to be broken up
  def canFitOnOneLine(
    string: String,
    fontMin: Int,
    resolutionWidth: Int,
    padding: Int,
    fontFamily: String
  ): Boolean = {

    val renderedText: Text = new Text(string)
    renderedText.setFont(Font.font(fontFamily, FontWeight.BOLD, fontMin))
    val effectiveWidth = resolutionWidth - padding * 2

    renderedText.getLayoutBounds.getWidth <= effectiveWidth
  }

  // Gets the largest font size within a certain range that can comfortably fit in the specified area
  def getFontSizeForLine(
    line: String,
    ppi: Int,
    resolutionWidth: Int,
    padding: Int,
    minFontSize: Int,
    maxFontSize: Int,
    fontFamily: String,
    twoLines: Boolean = false
  ): Int = {
    val font =

    if(twoLines) {
      val (firstLine, secondLine) = TextProcessor.cleaveSentence(line)

      return Math.min(
        getFontSizeForLine(firstLine, ppi, resolutionWidth, padding, minFontSize, maxFontSize, fontFamily),
        getFontSizeForLine(secondLine, ppi, resolutionWidth, padding, minFontSize, maxFontSize, fontFamily))
    }

    val effectiveWidth = resolutionWidth - (padding * 2)
    val eligibleFontSizes = (maxFontSize to minFontSize by -1).dropWhile { size =>
      getTextWidth(line, Font.font(fontFamily, FontWeight.BOLD, size)) > effectiveWidth
    } toList

    eligibleFontSizes match {
      case List() => 0
      case head :: tail => head
    }
  }

  def renderFuriganaFragment(
    textWithReading: TextWithReading,
    fontSize: Int,
    spacing: Int,
    ppi: Int,
    fontFamily: String
  ): Image = {

    val bigFont = Font.font(fontFamily, FontWeight.BOLD, fontSize)
    val smallFont = Font.font(fontFamily, FontWeight.BOLD, fontSize / 2)

    textWithReading match {
      case TextWithReading(txt, reading) =>

        val (baseWidth, baseHeight) = getTextDimensions(txt, bigFont)
        // We don't know if the reading includes furigana, so we just fake it with the base text.
        val (readingWidth, readingHeight) = if(reading != "") {
          getTextDimensions(reading, smallFont)
        } else {
          getTextDimensions(txt, smallFont)
        }

        val widthOfCanvas = Math.max(
          baseWidth,
          readingWidth
        )

        logger.debug("The width of %s and %s are %1.2f and %1.2f, so canvas width is %1.2f".format(txt, reading, baseWidth, readingWidth, widthOfCanvas))

        // TODO: Work out weird constant multiplication? Probably has something to do with DPI, but I have no idea why (or glyph height to width ratio)
        val heightScalingFactor = getFontHeightToWidthRatio(bigFont)
        val scaledFuriganaHeight: Double =  readingHeight * heightScalingFactor
        val scaledBaseHeight = baseHeight * heightScalingFactor

        val heightOfCanvas = scaledFuriganaHeight + scaledBaseHeight + spacing

        logger.debug("The height of the fragment (%s) is %1.2f, since the reading height, text height, and spacing are %1.2f, %1.2f, %d".format(txt, heightOfCanvas, readingHeight, baseHeight, spacing))

        implicit val canvasForText: Canvas = new Canvas(widthOfCanvas, heightOfCanvas)

        // The Kanji extend around 5 pixels from the base line of the font
        val baseYPosition = scaledFuriganaHeight + spacing + scaledBaseHeight - 6
        writeTextToCanvas(txt, canvasForText.getWidth / 2, baseYPosition)(canvasForText, bigFont)

        val readingYPosition = scaledFuriganaHeight
        writeTextToCanvas(reading, canvasForText.getWidth / 2, readingYPosition)(canvasForText, smallFont)

        val img = takePictureOfCanvas
        if(debug) {
          FileProcessor.saveImageToFile(img, UUID.randomUUID.toString + ".png")
        }
        img
    }
  }

  def joinFuriganaFragments(listOfFragments: List[(TextWithReading, Image)], fontSize: Int, fontFamily: String): Image = {
    val font = Font.font(fontFamily, FontWeight.BOLD, fontSize)
    val smallFont = Font.font(fontFamily, FontWeight.BOLD, fontSize / 2)

    listOfFragments match {
      // Special case on lines without Furigana
      case List(oneItem) => oneItem._2
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

            // The starting x position for the middle element's base text to align on requires a calculation depending on whether the base or furigana was larger
            var startingXForMiddle: Double = if(firstTextWidth > getTextWidth(firstText.reading, smallFont)) {
              firstTextWidth
            } else {
              firstTextWidth + firstFuriganaOverhang
            }

            (middle :+ last).foreach {
              case (txt, img) =>
                val strWidth = getTextWidth(txt.baseText, font)
                val overhang = (img.getWidth - strWidth) / 2
                gc.drawImage(img, startingXForMiddle - overhang, 0)
                startingXForMiddle += strWidth
            }


            val img = takePictureOfCanvas
            if(debug) {
              FileProcessor.saveImageToFile(img, listOfFragments.hashCode() + ".png")
            }
            img
        }
    }
  }

  // Warning: Assumes all images have the same height (maybe will fix later)
  def stackImagesWithSpacing(images: List[Image], spacing: Int): Image = {
    val (canvasWidth, canvasHeight) = (images.map(_.getWidth).max, images.map(_.getHeight).sum + (spacing * (images.length - 1)))
    implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)
    val gc = canvas.getGraphicsContext2D
    images.zipWithIndex foreach {
      case(image, index) =>
        val imageXPos = (canvasWidth - image.getWidth) / 2
        val imageYPos = (image.getHeight + spacing) * index
        gc.drawImage(image, imageXPos, imageYPos)
    }
    takePictureOfCanvas
  }

  def renderEnglishTextLine(topLine: String,  fontSize: Int, fontFamily: String, spacing: Int, bottomLine: Option[String] = None): Image = {
    implicit val font: Font = Font.font(fontFamily, FontWeight.BOLD, fontSize)
    bottomLine match {
      case Some(botLine) =>
        val (topWidth, topHeight) = getTextDimensions(topLine, font)
        val (botWidth, botHeight) = getTextDimensions(botLine, font)

        val canvasHeight = topHeight + botHeight + spacing
        val canvasWidth = Math.max(topWidth, botWidth)
        implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)

        writeTextToCanvas(topLine, canvasWidth / 2, topHeight * .85)
        writeTextToCanvas(botLine, canvasWidth / 2, canvasHeight - (botHeight * .42))
        takePictureOfCanvas
      case None =>
        val (canvasWidth, canvasHeight) = getTextDimensions(topLine, font)
        implicit val canvas: Canvas = new Canvas(canvasWidth, canvasHeight)

        writeTextToCanvas(topLine, canvasWidth / 2, canvasHeight * .75)
        takePictureOfCanvas
    }
  }

  def paintText(topText: Image, bottomText: Image, xDimension: Int, yDimension: Int, ppi: Int): Image = {
    implicit val canvas: Canvas = new Canvas(xDimension, yDimension)
    val gc = canvas.getGraphicsContext2D

    gc.setFill(Color.BLACK)
    gc.fillRect(0, 0, xDimension, yDimension)

    val thirdOfHeight = yDimension / 3

    val topYPosition = (thirdOfHeight - topText.getHeight) / 2
    val bottomYPosition = (thirdOfHeight * 2) + ((thirdOfHeight - bottomText.getHeight) / 2)

    val topXPosition = (xDimension - topText.getWidth) / 2
    val bottomXPosition = (xDimension - bottomText.getWidth) / 2

    gc.drawImage(topText, topXPosition, topYPosition)
    gc.drawImage(bottomText, bottomXPosition, bottomYPosition)

    takePictureOfCanvas
  }
}

trait GraphicsHelpers {
  protected val logger = GlobalContext.logger

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

  def takePictureOfCanvas(implicit canvas: Canvas, fill: Color = Color.TRANSPARENT): Image = {
    val snapshotParameters = new SnapshotParameters
    snapshotParameters.setFill(fill)
    canvas.snapshot(snapshotParameters, null)
  }

  def writeTextToCanvas(
    text: String, xPos: Double, yPos: Double,
    textAlign: TextAlignment = TextAlignment.CENTER, color: Color = Color.WHITE
  )(implicit canvas: Canvas, font: Font): Unit = {
    val gc = canvas.getGraphicsContext2D

    gc.setFont(font)
    gc.setTextAlign(textAlign)
    gc.setFill(color)

    gc.fillText(text, xPos, yPos)
  }
}