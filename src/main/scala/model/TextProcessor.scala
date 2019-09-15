package model

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

  /**
    * Divides a sentence as close to half as possible while respecting word boundaries
    * @param sentence An English or Japanese sentence, separated by spaces
    * @return A tuple containing the first half of the sentence and the bottom half of the sentence
    */
  def cleaveSentence(sentence: String): (String, String) = {
    if(sentence.contains('\\')) {
      val halves = sentence.split('\\')
      return (halves(0).trim, halves(1).trim)
    }

    // We continue to take words until the number of characters we've accumulated (taking into account spaces) is over
    // half of the sentence. Since we always want the top longer than the bottom, we move one element from the second half to the first
    var runningLength = 0
    val (firstHalf, secondHalf) =  sentence.split(' ').span { str =>
      runningLength += str.length + 1
      runningLength < sentence.length / 2
    } match {
      case (h, t) => (h ++ t.take(1), t.drop(1))
    }

    (firstHalf.mkString(" "), secondHalf.mkString(" "))
  }

}

case class TextWithReading(baseText: String, reading: String) {
  def toList: List[String] = List(baseText, reading)
}