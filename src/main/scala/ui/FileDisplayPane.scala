package ui

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.concurrent.Task
import javafx.geometry.{Insets, Pos}
import javafx.scene.control._
import javafx.scene.layout.{HBox, Priority, VBox}
import javafx.stage.{FileChooser, Stage}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.scene.text.Font
import model.{TextProcessor, TextRenderer}

import scala.collection.JavaConverters._
import scala.io.Source

class FileDisplayPane(labelText: String, val parent: ApplicationScene, allowSplitting: Boolean)(implicit stage: Stage) extends VBox(10.0) {
  var fontSize: Int = 0
  val textSize = 16.0

  setPadding(new Insets(0, 10, 0, 10))
  val label: Label = new Label(labelText) {
    setPadding(new Insets(10, 0, 0, 5))
    setFont(Font.font(textSize))
  }

  var fileRawLines: List[String] = List()
  val fileContents: ListView[String] = new ListView[String]() {
    setEditable(false)
  }
  fileContents.setCellFactory(_ => new ListCell[String] {
    override def updateItem(item: String, empty: Boolean): Unit = {
      super.updateItem(item, empty)
      if (item != null) {
        setText(TextProcessor.partitionLinesAndReadings(item)._1)
        setFont(Font.font(textSize))
      }
    }

    setOnMouseClicked(e => {
      parent.setPreviewImage(getIndex)
    })
  })

  val fontLabel = new Label("Font Size: ...") {
    setFont(Font.font(textSize))
  }
  val fontSlider: Slider = new Slider()
  fontSlider.setMin(0.0)
  fontSlider.setMax(0.0)
  fontSlider.valueProperty().addListener(new ChangeListener[Number] {
    override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
      updateFont(newValue.intValue())
    }
  })
  HBox.setHgrow(fontSlider, Priority.ALWAYS)
  val button: Button = new Button("Load File") {
    setOnAction(_ => fillTextAreaWithFileContents)
    setFont(Font.font(textSize))
  }
  val buttonBox: HBox = new HBox(10.0) {
    setAlignment(Pos.BASELINE_RIGHT)
    getChildren.addAll(fontLabel, fontSlider, button)
  }

  getChildren.addAll(label, fileContents, buttonBox)

  def fillTextAreaWithFileContents: Unit = {
    val fc = new FileChooser
    fc.setTitle("Choose a lyric file.")
    fc.getExtensionFilters.add(
      new ExtensionFilter("All Files", "*.*")
    )
    val chosenFile = fc.showOpenDialog(stage)
    if(chosenFile != null) {
      val lines = Source.fromFile(chosenFile, "UTF-8").getLines().toList
      fileRawLines = lines
      fileContents.getItems.setAll(lines: _*)
      parent.tryLinkingScrolls
      recalculateMaxFont()
    }
  }

  def getNthLine(n: Int): String = {
    fileContents.getSelectionModel.select(n)
    fileRawLines.lift(n) match {
      case Some(s) => s
      case _ => ""
    }
  }

  def getAllLines: List[String] = fileContents.getItems.iterator().asScala.toList

  def getScrollbar: Option[ScrollBar] = {
    fileContents.lookup(".scroll-bar") match {
      case sc: ScrollBar => Some(sc)
      case _ => None
    }
  }

  def recalculateMaxFont(): Unit = {
    fontSize = TextRenderer.getLargestFontSize(getAllLines.map(TextProcessor.partitionLinesAndReadings(_)._1), parent.rc, allowSplitting)
    fontSlider.setMax(fontSize)
    updateFont(fontSize)
  }

  private def updateFont(fontSize: Int): Unit = {
    this.fontSize = fontSize
    fontSlider.setValue(fontSize)
    fontLabel.setText(f"Font Size: $fontSize%02d")
    val waitAndUpdateImage = new Task[Int]() {
        override def call(): Int = {
          Thread.sleep(500)
          fontSize
        }
    }
    waitAndUpdateImage.setOnSucceeded(_ => {
        if(waitAndUpdateImage.getValue == this.fontSize) {
          parent.renderPreviewImage()
        }
      })

    new Thread(waitAndUpdateImage).start()
  }

  def getFontSize(): Int = fontSize
}