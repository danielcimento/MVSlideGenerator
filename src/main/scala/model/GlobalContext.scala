package model

object GlobalContext {
  lazy val usage: String =
    """
      |Usage: mvsg <english-input-file> <japanese-input-file>
      |   Command line arguments:
      |     -e <font-min> <font-max> : Sets the English font size range (Default: 80 to 100)
      |     -j <font-min> <font-max> : Sets the Japanese font size range (Default: 70 to 100)
      |     -r <width> <height> : Sets the resulting image resolution (Default: 1920 by 1080)
      |     -p <padding> : Sets the horizontal padding around lines (Default: 50 pixels)
      |     -f <typeface> : Sets the typeface. See note 1 (Default: Meiryo)
      |     -l <spacing> : Sets the line spacing for English text (Default: 50 pixels)
      |     -F <spacing> : Sets the spacing between Kanji and Furigana (Default: 15 pixels)
      |   Note 1: This application produced bolded text on slides, so if the font given does not have a bold counterpart
      |   then errors or unexpected behavior may occur during rendering.
    """.stripMargin
}
