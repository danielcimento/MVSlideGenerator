package ui.files

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleListProperty
import javafx.collections.{ListChangeListener, ObservableArray}
import javafx.scene.control.{ListCell, ListView}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text, TextFlow}
import model.{TextProcessor, TextWithReading}
import ui.Globals

class FileContentArea(onClickListener: Int => Unit) extends ListView[String] {
  val rawLines = new SimpleListProperty[String]()

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
        // TODO: Pick a more appropriate style/color
        formattedText.setFill(Color.DARKORANGE)
        // TODO: Add popup
        formattedText.setOnMouseEntered(_ => ())
        formattedText.setOnMouseExited(_ => ())
        formattedText
    })
    texts.foreach(txt => txt.setFont(Font.font(Globals.uiFont)))
    new TextFlow(texts: _*)
  }
}
