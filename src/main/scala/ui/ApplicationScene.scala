package ui

import javafx.scene.Scene
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority, RowConstraints}
import javafx.stage.Stage
import model.{GraphicsRenderer, RunConfig, TextProcessor, TextRenderer}

class ApplicationScene(val rc: RunConfig)(implicit stage: Stage) extends GridPane {
  var currentPreview = -1

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

  def setPreviewImage(lineNumber: Int): Unit = {
    currentPreview = lineNumber
    renderPreviewImage()
  }

  def renderPreviewImage(): Unit = {
    if(currentPreview >= 0) {
      val engLine = englishFileArea.getNthLine(currentPreview)
      val jpLine = japaneseFileArea.getNthLine(currentPreview)
      if(engLine.isEmpty || jpLine.isEmpty) {
        imageProcessingArea.displayWarning()
      } else {
        val previewImage = GraphicsRenderer.convertLinesToImage(engLine, jpLine, englishFileArea.getFontSize(), japaneseFileArea.getFontSize(), rc)
        imageProcessingArea.updatePreviewImage(previewImage)
      }
    }
  }

  def tryLinkingScrolls: Unit = {
    (japaneseFileArea.getScrollbar, englishFileArea.getScrollbar) match {
      case (Some(jsb), Some(esb)) => jsb.valueProperty().bindBidirectional(esb.valueProperty())
      case _ =>
    }
  }

  def createAllImages(outputPath: String) = {
    GraphicsRenderer.createAllImages(japaneseFileArea.getAllLines, englishFileArea.getAllLines, japaneseFileArea.getFontSize(), englishFileArea.getFontSize(), outputPath, rc)
  }
}
