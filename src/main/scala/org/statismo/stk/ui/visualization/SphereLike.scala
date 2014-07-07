package org.statismo.stk.ui.visualization

import org.statismo.stk.ui.visualization.props.{HasColorAndOpacity, HasRadius}
import org.statismo.stk.ui.EdtPublisher
import scala.swing.event.Event
import org.statismo.stk.core.geometry.Point3D

object SphereLike {

  case class CenterChanged(source: SphereLike) extends Event

}

trait SphereLike extends HasRadius with HasColorAndOpacity with EdtPublisher {
  private var _center: Point3D = Point3D(0, 0, 0)

  def center = _center

  def center_=(nc: Point3D) {
    if (nc != center) {
      _center = nc
      publishEdt(SphereLike.CenterChanged(this))
    }
  }
}
