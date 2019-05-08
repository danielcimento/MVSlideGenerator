package ui.images

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.{HBox, Priority}
import javafx.scene.text.Font
import javafx.stage.{DirectoryChooser, Stage}

// A nice little encapsulated class that has a label, a text field, and a button which allows users to search for their desired directory
class OutputPathSelection(implicit stage: Stage) extends HBox(10.0) {
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
    val chosenFile = dc.showDialog(stage)
    if(chosenFile != null) {
      outputPathField.setText(chosenFile.getAbsolutePath)
    }
  }
}
