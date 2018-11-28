package ui

import javafx.scene.Scene
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority, RowConstraints}
import javafx.stage.Stage
import model.{GraphicsRenderer, RunConfig, TextProcessor, TextRenderer}

class ApplicationScene(rc: RunConfig)(implicit stage: Stage) extends GridPane {
  // TODO: Replace with encapsulated object from model
  var englishFontSize = 0
  var japaneseFontSize = 0

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
  val englishFileArea = new FileDisplayPane("Top Text (English)", this)
  val japaneseFileArea = new FileDisplayPane("Bottom Text (Japanese)", this)
  add(englishFileArea, 0, 0)
  add(japaneseFileArea, 0, 1)

  val imageProcessingArea = new ImageProcessingArea(this)
  add(imageProcessingArea, 1, 0, 1, 2)

  def setPreviewImage(lineNumber: Int): Unit = {
    val engLine = englishFileArea.getNthLine(lineNumber)
    val jpLine = japaneseFileArea.getNthLine(lineNumber)
    val previewImage = GraphicsRenderer.convertLinesToImage(engLine, jpLine, englishFontSize, japaneseFontSize, rc)
    imageProcessingArea.updatePreviewImage(previewImage)
  }

  def tryLinkingScrolls: Unit = {
    (japaneseFileArea.getScrollbar, englishFileArea.getScrollbar) match {
      case (Some(jsb), Some(esb)) => jsb.valueProperty().bindBidirectional(esb.valueProperty())
      case _ =>
    }
  }

  def recalculateFontSizes = {
    japaneseFontSize = TextRenderer.getLargestFontSize(japaneseFileArea.getAllLines.map(TextProcessor.partitionLinesAndReadings(_)._1), rc)
    englishFontSize = TextRenderer.getLargestFontSize(englishFileArea.getAllLines, rc, true)
  }

  def createAllImages(outputPath: String) = {
    GraphicsRenderer.createAllImages(japaneseFileArea.getAllLines, englishFileArea.getAllLines, japaneseFontSize, englishFontSize, outputPath, rc)
  }
}
