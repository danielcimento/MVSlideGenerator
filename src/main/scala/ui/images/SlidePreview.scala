package ui.images

import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{Priority, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text}

class SlidePreview(parent: VBox) extends StackPane {
  def image(img: Image): Unit = {
    val slidePreview: ImageView = new ImageView(img)
    slidePreview.setFitWidth(this.getWidth - 20)
    slidePreview.setPreserveRatio(true)

    setStyle("-fx-background-color: transparent;")

    getChildren.setAll(slidePreview)
  }

  def blank(message: String): Unit = {
    val text = new Text(message) {
      wrappingWidthProperty().bind(parent.widthProperty().multiply(0.75))
      setFont(Font.font(40))
      setFill(Color.GRAY)

      override def isResizable: Boolean = true
    }

    setStyle("-fx-background-color: lightgray;")

    getChildren.setAll(text)
  }


}
