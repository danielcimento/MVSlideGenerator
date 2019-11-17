package ui.images

import java.io.File

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.{HBox, Priority}
import javafx.scene.text.Font
import javafx.stage.{DirectoryChooser, Stage}
import model.RunConfig
import ui.Globals
import ui.utils.StickyDirectoryChooser

// A nice little encapsulated class that has a label, a text field, and a button which allows users to search for their desired directory
class OutputPathSelection(implicit stage: Stage, rc: RunConfig) extends HBox(10.0) {
  private val _outputPath = new SimpleStringProperty()
  def outputPath: String = _outputPath.get()

  private val outputPathLabel = new Label("File Output Path:") {
    setFont(Font.font(Globals.uiFont))
  }
  private val outputPathField = new TextField()
  HBox.setHgrow(outputPathField, Priority.ALWAYS)

  private val outputPathSelect = new Button("...")
  outputPathSelect.setOnAction(_ => updateOutputPath())
  _outputPath.bind(outputPathField.textProperty())

  getChildren.addAll(outputPathLabel, outputPathField, outputPathSelect)

  private def updateOutputPath(): Unit = {
    val dc = new StickyDirectoryChooser(rc).withTitle("Choose an Output Directory")

    dc.showDialog(stage) match {
      case Some(file) => outputPathField.setText(file.getAbsolutePath)
      case None => ()
    }
  }

  def clear(): Unit = {
    outputPathField.setText("")
  }
}
