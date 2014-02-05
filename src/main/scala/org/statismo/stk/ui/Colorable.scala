package org.statismo.stk.ui

import scala.swing.event.Event
import java.awt.Color
import scala.swing.Publisher

object Colorable {
  case class AppearanceChanged(source: Colorable) extends Event
}

trait Colorable extends Publisher {
  private var _color: Color = Color.WHITE
  
  def color = {_color}
  def color_=(newColor: Color) = {
    if (_color != newColor) {
      _color = newColor
      publish(Colorable.AppearanceChanged(this))
    }
  }
  
  private var _opacity: Double = 1.0
  def opacity = {_opacity}
  def opacity_=(newOpacity: Double) = {
    if (_opacity != newOpacity) {
      _opacity = newOpacity
      publish(Colorable.AppearanceChanged(this))
    }
  }
}

