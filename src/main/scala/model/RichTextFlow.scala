package model

import javafx.scene.text.{Text, TextFlow}

class RichTextFlow extends TextFlow {
  var maximumLineWidth = 0.0
  var lineHeight = 0.0
  var lineCount = 1

  def addChild(text: Text): Unit = {
    lineHeight = Math.max(lineHeight, text.getLayoutBounds.getHeight)
    maximumLineWidth = Math.max(maximumLineWidth, text.getLayoutBounds.getWidth)
    println(s"Segment '${text.getText}' has width ${text.getLayoutBounds.getWidth}. Max line width is $maximumLineWidth")
    getChildren.add(text)
  }
}
