package model

case class DecoratedText(rawText: String, decorations: List[TextDecoration] = List()) {

}

object DecoratedText {
  type DecoratedLine = List[DecoratedText]
}

sealed trait TextDecoration
case object Italics extends TextDecoration
case object Underlined extends TextDecoration