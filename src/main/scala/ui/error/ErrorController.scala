package ui.error

import javafx.fxml.FXML
import javafx.scene.control.Label


class ErrorController {
  @FXML private var errorMessage: Label = null

  def setErrorText(text: String): Unit = {
    errorMessage.setText(text)
  }

  @FXML private def close(): Unit = {
    errorMessage.getScene.getWindow.hide()
  }
}