package ui.error

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.fxml.FXML
import javafx.scene.control.Label


class Controller {
  final private var counter = new SimpleIntegerProperty(1)
  @FXML private var label: Label = null

  @throws[Exception]
  def initialize(): Unit = {
    label.textProperty.bind(Bindings.format("Count: %s", counter))
    // uncomment the next line to demo exceptions in the start() method:
    // throw new Exception("Initializer exception");
  }

  @FXML private def safeHandler(): Unit = {
    counter.set(counter.get + 1)
  }

  @FXML
  @throws[Exception]
  private def riskyHandler(): Unit = {
    if (Math.random < 0.5) throw new RuntimeException("An unknown error occurred")
    safeHandler()
  }
}