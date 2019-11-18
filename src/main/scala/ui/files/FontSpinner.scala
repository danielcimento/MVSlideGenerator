package ui.files

import javafx.beans.property.{DoubleProperty, ReadOnlyObjectProperty, ReadOnlyProperty}
import javafx.geometry.Pos
import javafx.scene.control.{Label, Slider, Spinner}
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.layout.{HBox, Priority}
import javafx.scene.text.Font
import ui.Globals

class FontSpinner extends HBox {
  private val spinner = new Spinner[Int](0, 999, 60) {
    setEditable(true)
  }
  def fontSize: ReadOnlyObjectProperty[Int] = spinner.valueProperty()

  private val label = new Label("Font Size: ") {
    setFont(Font.font(Globals.uiFont))
  }

  HBox.setHgrow(spinner, Priority.NEVER)

  setAlignment(Pos.CENTER)
  setVisible(false)
  getChildren.addAll(label, spinner)
}
