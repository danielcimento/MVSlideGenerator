package ui

import javafx.concurrent.Task
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Label, ProgressBar, TextField}
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

  val progressStackpane = new StackPane()
  val creatingProgress = new ProgressBar()
  val progressLabel = new Label("Saving images...")
  progressStackpane.getChildren.addAll(creatingProgress, progressLabel)
  creatingProgress.setMaxWidth(Double.MaxValue)
  progressStackpane.setVisible(false)
  progressStackpane.setMaxHeight(Double.MaxValue)

  val createButton = new Button("Create Slides")

  val createGroup = new HBox(10.0)
  createGroup.getChildren.addAll(progressStackpane, createButton)
  createGroup.setAlignment(Pos.BASELINE_RIGHT)
  HBox.setHgrow(progressStackpane, Priority.ALWAYS)


  createButton.setOnAction(_ => {
    createButton.setDisable(true)
    progressStackpane.setVisible(true)
    parent.createAllImages(outputPath.getText, creatingProgress.progressProperty())
  })


  getChildren.addAll(label, emptySlidePreview, outputPathGroup, createGroup)

  def finishUpdatingImages(): Unit = {
      progressStackpane.setVisible(false)
      createButton.setDisable(false)
  }

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
