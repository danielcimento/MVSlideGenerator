package model

import javafx.scene.text.{Text, TextFlow}

class RichTextFlow extends TextFlow {
  var runningLineWidth = 0.0
  var maximumLineWidth = 0.0
  var lineHeight = 0.0
  var lineCount = 1

  def addChild(text: Text): Unit = {
    if (text.getText == System.lineSeparator()) {
      runningLineWidth = 0.0
    } else {
      lineHeight = Math.max(lineHeight, text.getLayoutBounds.getHeight)
      println(s"Segment '${text.getText}' has height ${text.getLayoutBounds.getHeight}. Max line height is $lineHeight")
      runningLineWidth += text.getLayoutBounds.getWidth
      maximumLineWidth = Math.max(runningLineWidth, maximumLineWidth)
    }
    println(s"Segment '${text.getText}' has width ${text.getLayoutBounds.getWidth}. Max line width is $maximumLineWidth")
    getChildren.add(text)
  }
}
