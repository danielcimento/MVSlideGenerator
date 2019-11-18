package model

import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontPosture, FontWeight, Text}
import model.RunConfig.Keys.FONT_FAMILY

case class DecoratedText(rawText: String, decorations: List[TextDecoration] = List()) {
  def createDecoratedText(fontSize: Int)(implicit rc: RunConfig): Text = {
    val fontPosture = if (decorations.contains(Italics)) FontPosture.ITALIC else FontPosture.REGULAR

    val text = new Text(rawText)
    text.setFont(Font.font(rc.getString(FONT_FAMILY), FontWeight.BOLD, fontPosture, fontSize))
    text.setFill(Color.WHITE)
    // TODO: Add more decorations
    text
  }
}

object LineBreak extends DecoratedText(System.lineSeparator(), List())

object DecoratedText {
  type DecoratedLine = List[DecoratedText]
}

sealed trait TextDecoration
case object Italics extends TextDecoration
case object Underlined extends TextDecoration