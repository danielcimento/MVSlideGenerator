package model

import model.DecoratedText.DecoratedLine

import scala.collection.mutable.ArrayBuffer

object TextProcessor {
  /**
    * This function takes in a string of Japanese text (with Kanji + reading pairs written as [Kanji|Reading]) and
    * partitions them into chunks of model.TextWithReading
    * @param strToSplit The Japanese text, formatted as above
    * @return A tuple containing the original line without the readings (i.e. [Kanji|Reading] -> Kanji) and the list of
    *         TextWithReadings for that line
    */
  def partitionLinesAndReadings(strToSplit: String): (String, List[TextWithReading]) = {
    val splitLine = strToSplit.split(Array('[', ']'))
    val partitionedText: List[TextWithReading] = splitLine.map { str =>
      if(str.contains('|')) {
        str.split('|') match {
          case Array(a, b) =>
            TextWithReading(a, b)
          case _ => throw new AssertionError("This string didn't contain exactly one | character:\n" + str)
        }
      } else {
        TextWithReading(str, "")
      }
    }.filter(_.baseText != "").toList

    val lineWithoutReadings: String = splitLine.map { str =>
      if(str.contains('|')) {
        str.takeWhile(_ != '|')
      } else {
        str
      }
    } reduceLeft(_ + _)

    (lineWithoutReadings, partitionedText)
  }

  // TODO: Add unit tests
  def decorateLineOfText(line: String): DecoratedLine = {
    val aggregatedList = new ArrayBuffer[DecoratedText]

    var escaped = false
    var lineOfRawText = new StringBuilder
    var currentDecorations: List[TextDecoration] = List()

    line.foreach({
      case '*' if !escaped =>
        if (currentDecorations.contains(Italics)) {
          aggregatedList.append(DecoratedText(lineOfRawText.toString(), currentDecorations))
          lineOfRawText = new StringBuilder
          currentDecorations = currentDecorations.filter(!_.equals(Italics))
        } else {
          aggregatedList.append(DecoratedText(lineOfRawText.toString(), currentDecorations))
          lineOfRawText = new StringBuilder
          currentDecorations = currentDecorations :+ Italics
        }
      case '_' if !escaped =>
        if (currentDecorations.contains(Underlined)) {
          aggregatedList.append(DecoratedText(lineOfRawText.toString(), currentDecorations))
          lineOfRawText = new StringBuilder
          currentDecorations = currentDecorations.filter(!_.equals(Underlined))
        } else {
          currentDecorations = currentDecorations :+ Underlined
        }
      case '\\' if !escaped =>
        escaped = true
      case c =>
        lineOfRawText.append(c)
        escaped = false
    })

    if (lineOfRawText.nonEmpty) {
      aggregatedList.append(DecoratedText(lineOfRawText.toString(), currentDecorations))
    }

    aggregatedList.toList
  }
}

case class TextWithReading(baseText: String, reading: String) {
  def toList: List[String] = List(baseText, reading)
}