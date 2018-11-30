package ui.files

import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.scene.control.{ListCell, ListView}
import javafx.scene.text.Font
import model.TextProcessor
import ui.Globals

class FileContentArea(onClickListener: Int => Unit) extends ListView[String] {
  setEditable(false)
  setDisable(true)
  disableProperty.bind(Bindings.isEmpty(getItems))

  setCellFactory(_ => new ListCell[String] {
    override def updateItem(item: String, empty: Boolean): Unit = {
      super.updateItem(item, empty)
      if (item != null) {
        setText(TextProcessor.partitionLinesAndReadings(item)._1)
        setFont(Font.font(Globals.uiFont))
      }
    }

    setOnMouseClicked(_ => {
      onClickListener(getIndex)
    })
  })
}
