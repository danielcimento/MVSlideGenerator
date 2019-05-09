package ui.images

import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout._
import javafx.scene.text.Font
import javafx.stage.Stage
import ui.{ApplicationScene, Globals}

class ImageProcessingArea(parent: ApplicationScene)(implicit stage: Stage) extends VBox(10.0) {
  setPadding(new Insets(0,10,0,0))

  private val labelAndSettingsBox: HBox = new HBox() {
    val label: Label = new Label("Slide Preview") {
      setPadding(new Insets(10, 0, 0, 5))
      setFont(Font.font(Globals.uiFont))
    }

    val filler = new Region()
    HBox.setHgrow(filler, Priority.ALWAYS)

    val settingsButton = new Button()
    settingsButton.setGraphic(new ImageView(new Image(getClass.getClassLoader.getResourceAsStream("cogs.png"), 24, 24, true, true)){
      setPadding(new Insets(10, 5, 0, 0))
    })

    setAlignment(Pos.CENTER)
    getChildren.addAll(label, filler, settingsButton)
  }


  val slidePreview = new SlidePreview(this)
  slidePreview.blank("A slide preview will appear here when you upload your lyric files and select a line.")
  VBox.setVgrow(slidePreview, Priority.ALWAYS)

  val outputPathSelection = new OutputPathSelection()

  val progressBar = new ImageSavingProgressBar()
  val createButton = new Button("Create Slides")

  val createGroup = new HBox(10.0)
  createGroup.getChildren.addAll(progressBar, createButton)
  createGroup.setAlignment(Pos.BASELINE_RIGHT)
  HBox.setHgrow(progressBar, Priority.ALWAYS)


  createButton.setOnAction(_ => {
    createButton.setDisable(true)
    progressBar.turnOn()
    parent.createAllImages(outputPathSelection.outputPath, progressBar.progressProperty)
  })


  getChildren.addAll(labelAndSettingsBox, slidePreview, outputPathSelection, createGroup)

  def updatePreviewImage(img: Image): Unit = {
    slidePreview.image(img)
  }

  def reset(): Unit = {
    progressBar.turnOff()
    createButton.setDisable(false)
  }

  def displayWarning(): Unit = {
    slidePreview.blank("Could not find corresponding lines in both files, so no preview can be rendered.")
  }
}
