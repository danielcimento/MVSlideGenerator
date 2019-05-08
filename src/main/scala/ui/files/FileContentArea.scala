package ui.files

import javafx.beans.binding.Bindings
import javafx.scene.control.{ListCell, ListView, Tooltip}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text, TextFlow}
import model.{TextProcessor, TextWithReading}
import ui.Globals

class FileContentArea(onClickListener: Int => Unit) extends ListView[String] {
  setEditable(false)
  setDisable(true)
  disableProperty.bind(Bindings.isEmpty(getItems))

  setCellFactory(_ => new ListCell[String] {
    override def updateItem(item: String, empty: Boolean): Unit = {
      super.updateItem(item, empty)
      if(item != null) {
        setGraphic(partitionsToTextFlow(TextProcessor.partitionLinesAndReadings(item)._2))
      }
    }

    setOnMouseClicked(_ => {
      onClickListener(getIndex)
    })
  })

  private def partitionsToTextFlow(partitions: List[TextWithReading]): TextFlow = {
    val texts = partitions.map({
      case TextWithReading(base, "") => new Text(base)
      case TextWithReading(base, reading) =>
        val formattedText = new Text(base)
        formattedText.setFill(Color.SLATEBLUE)
        formattedText.setUnderline(true)
        val readingTooltip = new Tooltip(reading) {
          setFont(Font.font(Globals.uiFont))
        }
        formattedText.setOnMouseEntered(_ => Tooltip.install(formattedText, readingTooltip))
        formattedText.setOnMouseExited(_ => Tooltip.uninstall(formattedText, readingTooltip))
        formattedText
    }).map(txt => { txt.setFont(Font.font(Globals.uiFont)); txt })
    new TextFlow(texts: _*)
  }
}
