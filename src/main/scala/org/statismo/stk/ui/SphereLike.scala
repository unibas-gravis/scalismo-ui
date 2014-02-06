package org.statismo.stk.ui

import scala.swing.Publisher
import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event

object SphereLike {
  case class CenterChanged(source: SphereLike) extends Event
}

trait SphereLike extends Radius with Colorable {

  private var _center: Point3D = Point3D(0,0,0)
  def center = { _center }
  def center_=(newCenter: Point3D) = {
    if (newCenter != _center) {
      _center = newCenter
      publish(SphereLike.CenterChanged(this))
    }
  }
}