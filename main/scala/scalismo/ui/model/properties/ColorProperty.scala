package scalismo.ui.model.properties

import java.awt.Color

object ColorProperty {
  val DefaultValue: Color = Color.WHITE
}

class ColorProperty(initialValue: => Color) extends NodeProperty[Color](initialValue) {
  def this() = this(ColorProperty.DefaultValue)
}

trait HasColor {
  def color: ColorProperty
}
