package ui

import javafx.geometry.{Insets, Pos}
import javafx.scene.control._
import javafx.scene.layout.{HBox, VBox}
import javafx.stage.{FileChooser, Stage}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.scene.text.Font
import model.TextProcessor

import scala.collection.JavaConverters._

import scala.io.Source

class FileDisplayPane(labelText: String, val parent: ApplicationScene)(implicit stage: Stage) extends VBox(10.0) {
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


  val buttonBox: HBox = new HBox() {
    val button: Button = new Button("Load File") {
      setOnAction(_ => fillTextAreaWithFileContents)
      setFont(Font.font(textSize))
    }
    setAlignment(Pos.BASELINE_RIGHT)
    getChildren.addAll(button)
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
    }
    parent.tryLinkingScrolls
    parent.recalculateFontSizes
  }

  def getNthLine(n: Int): String = {
    fileContents.getSelectionModel.select(n)
//    fileContents.scrollTo(n)
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