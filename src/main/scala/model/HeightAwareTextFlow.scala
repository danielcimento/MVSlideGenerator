package model

import javafx.scene.text.{Text, TextFlow}

class HeightAwareTextFlow extends TextFlow {
  var lineHeight = 0.0
  var lineCount = 1

  def addChild(text: Text): Unit = {
    lineHeight = Math.max(lineHeight, text.getLayoutBounds.getHeight)
    getChildren.add(text)
  }
}
