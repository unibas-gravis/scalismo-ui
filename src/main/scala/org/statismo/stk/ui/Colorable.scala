package org.statismo.stk.ui

import java.awt.Color

import scala.swing.event.Event

object Colorable {

  case class ColorChanged(source: Colorable) extends Event

}

trait Colorable extends EdtPublisher {
  private var _color: Color = Color.WHITE

  def color = {
    _color
  }

  def color_=(newColor: Color) = {
    if (_color != newColor) {
      _color = newColor
      publish(Colorable.ColorChanged(this))
    }
  }

  private var _opacity: Double = 1.0

  def opacity = {
    _opacity
  }

  def opacity_=(newOpacity: Double) = {
    if (_opacity != newOpacity) {
      _opacity = newOpacity
      publish(Colorable.ColorChanged(this))
    }
  }
}

