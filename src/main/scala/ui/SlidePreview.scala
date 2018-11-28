package ui

import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.layout.{StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.{Font, Text}

class SlidePreview(parent: VBox) extends StackPane {
  val text = new Text("A slide preview will appear here when you upload your lyric files and select a line.") {
    wrappingWidthProperty().bind(parent.widthProperty().multiply(0.75))
    setFont(Font.font(40))
    setFill(Color.GRAY)

    override def isResizable: Boolean = true
  }

  getChildren.addAll(text)
  setStyle("-fx-background-color: lightgray;")

}
