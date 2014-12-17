package org.statismo.stk.ui.visualization

import org.statismo.stk.core.geometry.{_3D, Point}
import org.statismo.stk.ui.visualization.props.{HasColorAndOpacity, HasRadius}
import org.statismo.stk.ui.EdtPublisher
import scala.swing.event.Event

object SphereLike {

  case class CenterChanged(source: SphereLike) extends Event

}

trait SphereLike extends HasRadius with HasColorAndOpacity with EdtPublisher {
  private var _center: Point[_3D] = Point(0, 0, 0)

  def center = _center

  def center_=(nc: Point[_3D]) {
    if (nc != center) {
      _center = nc
      publishEdt(SphereLike.CenterChanged(this))
    }
  }
}
