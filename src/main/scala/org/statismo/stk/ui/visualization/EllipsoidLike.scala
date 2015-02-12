package org.statismo.stk.ui.visualization

import org.statismo.stk.core.geometry.{_3D, Point}
import org.statismo.stk.ui.visualization.props.{HasRotation, HasColorAndOpacity, HasRadiuses}
import org.statismo.stk.ui.EdtPublisher
import scala.swing.event.Event

object EllipsoidLike {

  case class CenterChanged(source: EllipsoidLike) extends Event

}

trait EllipsoidLike extends HasRadiuses[_3D] with HasColorAndOpacity with HasRotation with EdtPublisher {
  private var _center: Point[_3D] = Point(0, 0, 0)

  def center = _center

  def center_=(nc: Point[_3D]) {
    if (nc != center) {
      _center = nc
      publishEdt(EllipsoidLike.CenterChanged(this))
    }
  }
}
