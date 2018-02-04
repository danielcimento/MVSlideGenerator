import javafx.scene.image.Image

object TextRenderer {
  private val logger = GlobalContext.logger

  def convertJapaneseLinesToImages(japaneseLines: List[String], rc: RunConfig): List[Image] = {
    val getFontSize: String => Int = GraphicsRenderer.getFontSizeForLine(_, rc.ppi, rc.resolutionWidth, rc.padding, rc.jpMinFont, rc.jpMaxFont, rc.typeface)

    // We want to know what the largest Japanese font size we can use is, so we go through each line of text
    // (x._1 => line), and calculate what the font size for that line should be, within our given bounds
    // Then, we take the minimum font size, so that all our lines have the same font
    val rawJapaneseLines = japaneseLines.map(TextProcessor.partitionLinesAndReadings).map(_._1)
    val largestFontSizes = rawJapaneseLines.map(getFontSize(_))

    val (jpFontSize, twoLines): (Int, Boolean) = if(largestFontSizes.contains(0)) {
      val minSize = rawJapaneseLines.map { sentence =>
          val (topLine, botLine) = TextProcessor.cleaveSentence(sentence)
          val topSize = getFontSize(topLine)
          val botSize = getFontSize(botLine)
          Math.min(topSize, botSize)
      }.min
      (minSize, true)
    } else {
      (largestFontSizes.min, false)
    }

    logger.debug("Settled on font size %d".format(jpFontSize))

    val furiganaRender: TextWithReading => Image = x => GraphicsRenderer.renderFuriganaFragment(x, jpFontSize, rc.furiganaSpacing, rc.ppi, rc.typeface)

    if(twoLines) {
      japaneseLines.map { sentence =>
        val (topLine, botLine) = TextProcessor.cleaveSentence(sentence)
        val lines = List(topLine, botLine)
        val partitions = lines.map(TextProcessor.partitionLinesAndReadings)
        val images = partitions.map {
          case (_, readings) =>
            val furiganaFragments = readings.map(furiganaRender(_))
            GraphicsRenderer.joinFuriganaFragments(readings.zip(furiganaFragments), jpFontSize, rc.typeface)
        }
        GraphicsRenderer.stackImagesWithSpacing(images, rc.lineSpacing)
      }
    } else {
      // First, we divide each Japanese line into a (line, readings) pair,
      // where the readings are the chunks of (Kanji + Furigana) or (Kana + EmptyString)
      // and the line is the original line with the furigana formatting removed (i.e. the whole "base line" of text)
      val partitions: List[(String, List[TextWithReading])] = japaneseLines.map(TextProcessor.partitionLinesAndReadings)
      logger.debug("Decided on minimal font size %d, printing on 1 line".format(jpFontSize))

      // Next, we build the images of the Japanese text. To do so, we turn each (Kanji, Reading) pair into a slice of the
      // sentence. We put together all the slices, taking into account overhang
      partitions.map {
        case (jpLine, textAndFurigana) =>
          val furiganaFragments = textAndFurigana.map(GraphicsRenderer.renderFuriganaFragment(_, jpFontSize, rc.furiganaSpacing, rc.ppi, rc.typeface))
          GraphicsRenderer.joinFuriganaFragments(textAndFurigana.zip(furiganaFragments), jpFontSize, rc.typeface)
      }
    }
  }


  def convertEnglishLinesToImages(englishLines: List[String], rc: RunConfig): List[Image] = {

    // First we calculate the English font size to be used. If we know that an English line needs to be broken into
    // two lines (due to spacing restriction), we calculate the largest possible font size when it is evenly divided
    // into two lines. Once we have each one calculated, we take the min, so that all can have the same size.
    val engFontSize: Int = englishLines.map { line =>
        val needsTwoLines = !GraphicsRenderer.canFitOnOneLine(line, rc.engMinFont, rc.resolutionWidth, rc.padding, rc.typeface)
        val fontSize = GraphicsRenderer.getFontSizeForLine(line, rc.ppi, rc.resolutionWidth, rc.padding, rc.engMinFont, rc.engMaxFont, rc.typeface, needsTwoLines)
        logger.debug("Line (%s) requires font size of %d".format(line, fontSize) + (if(needsTwoLines) " to fit on two lines." else "."))
        fontSize
    }.min

    logger.debug("Decided on English font size %d".format(engFontSize))

    // Next, we create the English translation images. The way we do so is by cleaving our sentence (if our chosen font
    // size requires it) and rendering the top and bottom lines above each other.
    englishLines.map { engLine =>
        val engImage: Image = if(GraphicsRenderer.canFitOnOneLine(engLine, engFontSize, rc.resolutionWidth, rc.padding, rc.typeface)) {
          GraphicsRenderer.renderEnglishTextLine(engLine, engFontSize, rc.typeface, rc.lineSpacing)
        } else {
          val (topLine, botLine) = TextProcessor.cleaveSentence(engLine)
          GraphicsRenderer.renderEnglishTextLine(topLine, engFontSize, rc.typeface, rc.padding, Some(botLine))
        }
        engImage
    }
  }
}
