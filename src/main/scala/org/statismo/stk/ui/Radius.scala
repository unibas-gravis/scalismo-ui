package org.statismo.stk.ui

import scala.swing.event.Event

object Radius {
  case class RadiusChanged(source: Radius) extends Event
}

trait Radius extends EdtPublisher {

  private var _radius: Float = 0
  def radius = { _radius }
  def radius_=(newRadius: Float) = {
    if (newRadius != _radius) {
      _radius = newRadius
      publish(Radius.RadiusChanged(this))
    }
  }
}