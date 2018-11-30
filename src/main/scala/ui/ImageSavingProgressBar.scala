package ui

import javafx.beans.property.DoubleProperty
import javafx.scene.control.{Label, ProgressBar}
import javafx.scene.layout.StackPane

class ImageSavingProgressBar extends StackPane {
  val creatingProgress = new ProgressBar()
  val progressLabel = new Label("Saving images...")
  creatingProgress.setMaxWidth(Double.MaxValue)

  getChildren.addAll(creatingProgress, progressLabel)
  setVisible(false)
  setMaxHeight(Double.MaxValue)

  def progressProperty: DoubleProperty = creatingProgress.progressProperty()

  def turnOn(): Unit = {
    setVisible(true)
  }

  def turnOff(): Unit = {
    progressProperty.unbind()
    progressProperty.setValue(0.0)
    setVisible(false)
  }
}
