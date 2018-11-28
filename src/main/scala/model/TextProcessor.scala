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
    // Separate each word and calculate the halfway point of the sentence
    val listOfWords = sentence.split(' ')
    val halfOfSentence = sentence.length / 2

    // Next, we continue to take words until the characters we've accumulated (taking into account spaces) is just around
    // half of the sentence. Because sometimes a large word will straddle that boundary, we use (str.length / 2) to calculate
    // the cost of taking it, so that the top line isn't weighted to be shorter than the bottom
    var wordsTakenSoFar = 0
    val (firstHalf, secondHalf) = listOfWords.span { str =>
      val underHalf = wordsTakenSoFar + (str.length / 2) <= halfOfSentence
      wordsTakenSoFar += str.length + 1
      underHalf
    }

    (firstHalf.mkString(" "), secondHalf.mkString(" "))
  }

}

case class TextWithReading(baseText: String, reading: String) {
  def toList: List[String] = List(baseText, reading)
}