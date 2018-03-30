package scalismo.ui.control

import java.awt.Color

import scalismo.ui.control.BackgroundColor.event.BackgroundColorChanged
import scalismo.ui.event.{Event, ScalismoPublisher}
import scalismo.ui.rendering.util.VtkUtil

object BackgroundColor {

  object event {

    case class BackgroundColorChanged(source: BackgroundColor) extends Event

  }

}

class BackgroundColor extends ScalismoPublisher {

  def value: Color = _value

  def value_=(newColor: Color): Unit = {
    _value = newColor
    _vtkValue = VtkUtil.colorToArray(_value)
    publishEvent(BackgroundColorChanged(this))
  }

  private var _value: Color = Color.BLACK
  private var _vtkValue = VtkUtil.colorToArray(_value)

  def vtkValue: Array[Double] = _vtkValue
}
