package ui.images

import java.io.File

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.{HBox, Priority}
import javafx.scene.text.Font
import javafx.stage.{DirectoryChooser, Stage}
import model.RunConfig
import model.RunConfig.Keys.LAST_OPENED_DIRECTORY
import ui.Globals

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
    val dc = new DirectoryChooser
    dc.setTitle("Choose an Output Directory")
    var lastDirectory = new File(rc.getString(LAST_OPENED_DIRECTORY))
    while(!lastDirectory.isDirectory) {
      lastDirectory = lastDirectory.getParentFile
    }
    if(lastDirectory != null) {
      dc.setInitialDirectory(lastDirectory)
      if(lastDirectory.getAbsolutePath != rc.getString(LAST_OPENED_DIRECTORY)) {
        rc.updateProperty(LAST_OPENED_DIRECTORY, lastDirectory.getAbsolutePath)
      }
    }

    val chosenFile = dc.showDialog(stage)

    if(chosenFile != null) {
      val pathToSave = chosenFile.getParent
      if(pathToSave != null) {
        rc.updateProperty(LAST_OPENED_DIRECTORY, pathToSave)
      }

      outputPathField.setText(chosenFile.getAbsolutePath)
    }
  }

  def clear(): Unit = {
    outputPathField.setText("")
  }
}
