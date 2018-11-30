package ui

import javafx.beans.property.{IntegerProperty, SimpleIntegerProperty}
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
  private val _fontSize: IntegerProperty = new SimpleIntegerProperty(0)
  def fontSize: Int = _fontSize.getValue

  // When the font size is changes, we want to wait a bit, then render a new preview (to prevent stuttering renders)
  _fontSize.addListener((_, _, newVal) => {
    // First, whenever our font size changes, start a new task that just waits .5 seconds in the background
    val waitAndUpdateImage = new Task[Unit]() {
      override def call(): Unit = {
        Thread.sleep(500)
      }
    }
    // When those .5 seconds pass, if the value we had previously updated to is still our current font value
    // (i.e. user stopped moving slider), then we render a new preview image
    waitAndUpdateImage.setOnSucceeded(_ => {
      if(newVal.intValue() == this._fontSize.getValue) {
        parent.renderPreviewImage()
      }
    })

    new Thread(waitAndUpdateImage).start()
  })

  setPadding(new Insets(0, 10, 0, 10))
  val label: Label = new Label(labelText) {
    setPadding(new Insets(10, 0, 0, 5))
    setFont(Font.font(Globals.uiFont))
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
        setFont(Font.font(Globals.uiFont))
      }
    }

    setOnMouseClicked(_ => {
      parent.setPreviewImage(getIndex)
    })
  })

  val fontSlider: FontSlider = new FontSlider
  _fontSize.bind(fontSlider.fontValue)
  fontSlider.visibleProperty().bind(fontSlider.maxProperty.greaterThan(0))

  val button: Button = new Button("Load File") {
    setOnAction(_ => fillTextAreaWithFileContents)
    setFont(Font.font(Globals.uiFont))
  }


  val buttonBox: HBox = new HBox(10.0) {
    setAlignment(Pos.CENTER)
    getChildren.addAll(fontSlider, button)
  }
  HBox.setHgrow(fontSlider, Priority.ALWAYS)

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
      val maxFontSize = TextRenderer.getLargestFontSize(getAllLines.map(TextProcessor.partitionLinesAndReadings(_)._1), parent.rc, allowSplitting)
      fontSlider.updateMaxSize(maxFontSize)
      fontSlider.updateFont(maxFontSize)
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
}