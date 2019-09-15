package ui

import javafx.beans.property.DoubleProperty
import javafx.concurrent.Task
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority, RowConstraints}
import javafx.stage.Stage
import model._
import ui.files.FileDisplayPane
import ui.images.ImageProcessingArea

class ApplicationScene(implicit stage: Stage, rc: RunConfig) extends GridPane {
  // Defines the line of the input files which we want to render a preview of
  private var currentPreview = -1

  getColumnConstraints.addAll(
    new ColumnConstraints() {
      setPercentWidth(40)
    },
    new ColumnConstraints() {
      setPercentWidth(60)
      setHgrow(Priority.ALWAYS)
    }
  )
  getRowConstraints.add(new RowConstraints() {
    setPercentHeight(50)
  })

  val englishFileArea = new FileDisplayPane("Top Text (English)", this, true)
  val japaneseFileArea = new FileDisplayPane("Bottom Text (Japanese)", this, false)
  add(englishFileArea, 0, 0)
  add(japaneseFileArea, 0, 1)

  val imageProcessingArea = new ImageProcessingArea(this)
  add(imageProcessingArea, 1, 0, 1, 2)

  // Separate from the renderPreviewImage method, because sometimes we don't want to change the line that's actually being interacted with
  def setPreviewImage(lineNumber: Int): Unit = {
    currentPreview = lineNumber
    renderPreviewImage()
  }

  // If we have a line selected by the user, try to render a preview image for it
  def renderPreviewImage(): Unit = {
    englishFileArea.selectNthLine(currentPreview)
    japaneseFileArea.selectNthLine(currentPreview)
    (englishFileArea.getNthLine(currentPreview), japaneseFileArea.getNthLine(currentPreview), japaneseFileArea.getNthLine(currentPreview + 1)) match {
      case (Some(englishLine), Some(japaneseLine), preview@_) =>
        val previewImage = GraphicsRenderer.convertLinesToImage(englishLine, japaneseLine, preview.getOrElse(""), englishFileArea.fontSize, japaneseFileArea.fontSize)
        imageProcessingArea.updatePreviewImage(previewImage)
      // If we couldn't find the line in question, but the user did select something, we display a warning
      case _ if currentPreview != -1 => imageProcessingArea.displayWarning()
      case _ => imageProcessingArea.reset()
    }
  }

  // We want both panes to scroll simultaneously
  def tryLinkingScrolls(): Unit = {
    (japaneseFileArea.getScrollbar, englishFileArea.getScrollbar) match {
      case (Some(jsb), Some(esb)) => jsb.valueProperty().bindBidirectional(esb.valueProperty())
      case _ =>
    }
  }

  // This is the method of the occasion. Does pretty much what the application was meant to do
  def createAllImages(outputPath: String, outputListener: DoubleProperty): Unit = {
    if(outputPath.isEmpty) {
      new Alert(
        AlertType.WARNING,
        "MVSlideGenerator will not save images to an empty directory. This is to prevent littering your root directory with images. Saving images to the empty directory is usually not desired."
      ).showAndWait()
      imageProcessingArea.reset()
    } else {
      // This is the stuff that actually gets done.
      // We convert it to a helper function because we want to
      // prompt confirmation if lines are mismatched.
      def execute(): Unit = {
        // Render all the images (needs to be on the same thread since we're using JavaFX assets)
        val images = GraphicsRenderer.createAllImages(japaneseFileArea.getLines, englishFileArea.getLines, japaneseFileArea.fontSize, englishFileArea.fontSize)(rc)
        // Then, create a background task to write all those files to the disk, updating the progress parameter as it goes
        val saveImages = new Task[Unit]() {
          override def call(): Unit = {
            val total = images.size
            images.zipWithIndex.foreach {
              case (img, i) =>
                FileProcessor.saveImageToFile(img, outputPath, f"image$i%04d.png")
                updateProgress(i, total)
            }
          }
        }
        outputListener.bind(saveImages.progressProperty)
        saveImages.setOnSucceeded(_ => cleanUpAfterImageCreation())
        new Thread(saveImages).start()
      }
      if(japaneseFileArea.getLines.size != englishFileArea.getLines.size) {
        val mismatchWarning = new Alert(
          AlertType.CONFIRMATION,
          "Your files have a mis-matched number of lines, so some slides will not have text on one half of the image. Would you still like to proceed?"
        )
        mismatchWarning.showAndWait().ifPresent {
          case ButtonType.OK => execute()
          case _ => imageProcessingArea.reset()
        }
      } else {
        execute()
      }
    }
  }

  def cleanUpAfterImageCreation(): Unit = {
    currentPreview = -1
    englishFileArea.reset()
    japaneseFileArea.reset()
    imageProcessingArea.reset()
  }

  def clearOutputPath(): Unit = {
    imageProcessingArea.clearOutputPath()
  }
}
