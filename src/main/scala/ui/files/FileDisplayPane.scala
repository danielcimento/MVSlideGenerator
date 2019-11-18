package ui.files

import java.io.File

import javafx.beans.property._
import javafx.concurrent.Task
import javafx.geometry.{Insets, Pos}
import javafx.scene.control._
import javafx.scene.layout.{HBox, Priority, VBox}
import javafx.scene.text.Font
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{FileChooser, Stage}
import model.{RunConfig, TextProcessor, TextRenderer}
import ui.{ApplicationScene, Globals}
import ui.Globals.tryWithResource
import model.RunConfig.Keys._
import ui.utils.StickyFileChooser

import scala.collection.JavaConverters._
import scala.io.Source

class FileDisplayPane(labelText: String, val parent: ApplicationScene, allowSplitting: Boolean)(implicit stage: Stage, rc: RunConfig) extends VBox(10.0) {
  // This is the primary property that needs to be visible to the parent scene
  val _fontSpinner = new FontSpinner

  private val _fontSize: ReadOnlyObjectProperty[Int] = _fontSpinner.fontSize
  def fontSize: Int = _fontSize.getValue

  private val _lines: ListProperty[String] = new SimpleListProperty[String]()
  def getLines: List[String] = _lines.get().asScala.toList
  private var _lastOpenedFilename: Option[String] = None

  // When the font size is changed, we want to wait a bit, then render a new preview (to prevent stuttering renders)
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

  // First we do some nice padding to make the labels look nicer
  setPadding(new Insets(0, 10, 0, 10))
  val label: Label = new Label(labelText) {
    setPadding(new Insets(10, 0, 0, 5))
    setFont(Font.font(Globals.uiFont))
  }

  // These are the two components mainly responsible for manipulating the exposed properties
  val fileContents: FileContentArea = new FileContentArea(parent.setPreviewImage)
  _lines.bind(fileContents.itemsProperty())

  // The row below the list of lines which allows user interaction
  val toolRow: HBox = new HBox(10.0) {
    val button: Button = new Button("Load File") {
      setOnAction(_ => fillTextAreaWithFileContents())
      setFont(Font.font(Globals.uiFont))
    }

    setAlignment(Pos.CENTER_RIGHT)
    getChildren.addAll(_fontSpinner, button)
  }

  getChildren.addAll(label, fileContents, toolRow)

  def fillTextAreaWithFileContents(): Unit = {
    val fc = new StickyFileChooser(rc).withTitle("Choose a lyric file.")
    fc.getExtensionFilters.add(
      new ExtensionFilter("All Files", "*.*")
    )

    fc.showOpenDialog(stage) match {
      case Some(file) =>
        tryWithResource(Source.fromFile(file, "UTF-8")) { textSource =>
        fileContents.getItems.setAll(textSource.getLines().map(_.trim).filter(_.nonEmpty).toList: _*)

        }
        parent.tryLinkingScrolls()
        if(!_fontSpinner.isVisible) {
          _fontSpinner.setVisible(true)
        }
      case None => ()
    }
  }

  def reset(): Unit = {
    fileContents.getItems.clear()
  }

  def getNthLine(n: Int): Option[String] = {
    // Highlight the line being queried (so the user can see which lines are being rendered)
    getLines.lift(n)
  }

  def selectNthLine(n: Int): Unit = {
      fileContents.getSelectionModel.select(n)
  }

  def getScrollbar: Option[ScrollBar] = {
    fileContents.lookup(".scroll-bar") match {
      case sc: ScrollBar => Some(sc)
      case _ => None
    }
  }
}