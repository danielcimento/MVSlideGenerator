package ui

import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, Priority, StackPane, VBox}
import javafx.scene.paint.{Color, Paint}
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.stage.{DirectoryChooser, FileChooser, Stage}
import javafx.stage.FileChooser.ExtensionFilter

import scala.io.Source

class ImageProcessingArea(parent: ApplicationScene)(implicit stage: Stage) extends VBox(10.0) {
  setPadding(new Insets(0,10,0,0))

  val label: Label = new Label("Slide Preview") {
    setPadding(new Insets(10, 0, 0, 5))
    setFont(Font.font(16.0))
  }

  val emptySlidePreview = new SlidePreview(this)
  VBox.setVgrow(emptySlidePreview, Priority.ALWAYS)


  val outputPathLabel = new Label("File Output Path:") {
    setFont(Font.font(16.0))
  }
  val outputPath = new TextField()
  HBox.setHgrow(outputPath, Priority.ALWAYS)

  val outputPathSelect = new Button("...")
  outputPathSelect.setOnAction(_ => updateOutputPath())

  val outputPathGroup = new HBox(10.0)
  outputPathGroup.getChildren.addAll(outputPathLabel, outputPath, outputPathSelect)

  val createButton = new Button("Create Slides")
  createButton.setOnAction(_ => {
    parent.createAllImages(outputPath.getText)
  })

  val createGroup = new HBox(10.0)
  createGroup.getChildren.addAll(createButton)
  createGroup.setAlignment(Pos.BASELINE_RIGHT)

  getChildren.addAll(label, emptySlidePreview, outputPathGroup, createGroup)

  def updatePreviewImage(img: Image): Unit = {
    val slidePreviewArea = new StackPane()
    val slidePreview: ImageView = new ImageView(img)
    slidePreview.setFitWidth(this.getWidth - 20)
    slidePreview.setPreserveRatio(true)

    slidePreviewArea.getChildren.addAll(slidePreview)
    VBox.setVgrow(slidePreviewArea, Priority.ALWAYS)

    getChildren.setAll(label, slidePreviewArea, outputPathGroup, createGroup)
  }

  def updateOutputPath(): Unit = {
    val dc = new DirectoryChooser
    dc.setTitle("Choose and Output Directory")
    val chosenFile = dc.showDialog(stage)
    if(chosenFile != null) {
      outputPath.setText(chosenFile.getAbsolutePath)
    }
  }

  def displayWarning(): Unit = {
    emptySlidePreview.text.setText("Could not find corresponding lines in both files, so no preview can be rendered.")
    getChildren.setAll(label, emptySlidePreview, outputPathGroup, createGroup)
  }
}
