package scalismo.ui.visualization

import scalismo.geometry.{ Point, _3D }
import scalismo.ui.EdtPublisher
import scalismo.ui.visualization.props.{ HasColorAndOpacity, HasLineWidth, HasRadiuses, HasRotation }

import scala.swing.event.Event

object EllipsoidLike {

  case class CenterChanged(source: EllipsoidLike) extends Event

}

trait EllipsoidLike extends HasRadiuses[_3D] with HasColorAndOpacity with HasRotation with HasLineWidth with EdtPublisher {
  private var _center: Point[_3D] = Point(0, 0, 0)

  def center = _center

  def center_=(nc: Point[_3D]): Unit = {
    if (nc != center) {
      _center = nc
      publishEdt(EllipsoidLike.CenterChanged(this))
    }
  }
}
