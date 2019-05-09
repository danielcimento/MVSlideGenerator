package ui.files

import javafx.beans.property.{DoubleProperty, IntegerProperty, SimpleIntegerProperty}
import javafx.geometry.Pos
import javafx.scene.control.{Label, Slider}
import javafx.scene.layout.{HBox, Priority}
import javafx.scene.text.Font
import ui.Globals

// A widget that holds a font value and shows a label displaying that font's value
class FontSlider extends HBox {
  val fontValue: IntegerProperty = new SimpleIntegerProperty(0)

  private val fontLabel = new Label() {
    textProperty.bind(fontValue.asString("Font Size %02d"))
    setFont(Font.font(Globals.uiFont))
  }

  private val fontSlider: Slider = new Slider() {
    setMin(0.0)
    setMax(0.0)
    valueProperty().bindBidirectional(fontValue)
  }
  HBox.setHgrow(fontSlider, Priority.ALWAYS)

  val maxProperty: DoubleProperty = fontSlider.maxProperty()
  visibleProperty().bind(fontSlider.maxProperty.greaterThan(0))

  def updateMaxSize(newMax: Int): Unit = fontSlider.setMax(newMax)
  def updateFont(newFont: Int): Unit = fontSlider.setValue(newFont)

  setAlignment(Pos.CENTER)
  getChildren.addAll(fontLabel, fontSlider)
}
