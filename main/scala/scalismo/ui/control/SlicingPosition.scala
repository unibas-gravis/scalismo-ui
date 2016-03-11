package scalismo.ui.control

import scalismo.geometry.{ Point, Point3D }
import scalismo.ui.control.SlicingPosition.event
import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.{ BoundingBox, Scene }
import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view.{ PerspectivesPanel, ScalismoFrame, ViewportPanel }

object SlicingPosition {

  object event {

    case class SlicesVisibleChanged(source: SlicingPosition) extends Event

    case class IntersectionsVisibleChanged(source: SlicingPosition) extends Event

    case class OpacityChanged(source: SlicingPosition) extends Event

    case class PointChanged(source: SlicingPosition, previous: Point3D, current: Point3D) extends Event

    case class BoundingBoxChanged(source: SlicingPosition) extends Event

  }

}

class SlicingPosition(val scene: Scene, val frame: ScalismoFrame) extends ScalismoPublisher {

  private var _slicesVisible = GlobalSettings.get[Boolean](GlobalSettings.Keys.SlicesVisible).getOrElse(false)

  def slicesVisible = _slicesVisible

  def slicesVisible_=(nv: Boolean) = {
    if (_slicesVisible != nv) {
      _slicesVisible = nv
      GlobalSettings.set(GlobalSettings.Keys.SlicesVisible, nv)
      publishEvent(event.SlicesVisibleChanged(this))
      //scene.publishEdt(Scene.SlicingPosition.SlicesVisibleChanged(this))
      //scene.publishEdt(Scene.VisibilityChanged(scene))
    }
  }

  private var _intersectionsVisible = GlobalSettings.get[Boolean](GlobalSettings.Keys.IntersectionsVisible).getOrElse(false)

  def intersectionsVisible = _intersectionsVisible

  def intersectionsVisible_=(nv: Boolean) = {
    if (_intersectionsVisible != nv) {
      _intersectionsVisible = nv
      GlobalSettings.set(GlobalSettings.Keys.IntersectionsVisible, nv)
      publishEvent(event.IntersectionsVisibleChanged(this))
      //scene.publishEdt(Scene.SlicingPosition.IntersectionsVisibleChanged(this))
      //scene.publishEdt(Scene.VisibilityChanged(scene))
    }
  }

  private var _opacity = Math.max(0.0, Math.min(1.0, GlobalSettings.get[Double](GlobalSettings.Keys.SlicesOpacity).getOrElse(0.0)))

  def opacity = _opacity

  def opacity_=(nv: Double) = {
    val sane = Math.max(0.0, Math.min(1.0, nv))
    if (_opacity != sane) {
      _opacity = sane
      GlobalSettings.set(GlobalSettings.Keys.SlicesOpacity, sane)
      publishEvent(event.OpacityChanged(this))
    }
  }

  private var _point: Point3D = Point3D(0, 0, 0)

  def point = {
    _point
  }

  def point_=(np: Point3D) = {
    if (_point != np) {
      val prev = _point
      _point = np
      publishEvent(event.PointChanged(this, np, prev))
    }
  }

  def x = point(0)

  def y = point(1)

  def z = point(2)

  def x_=(nv: Float) = {
    val sv = Math.min(Math.max(boundingBox.xMin, nv), boundingBox.xMax)
    if (x != sv) {
      point_=(Point(sv, y, z))
    }
  }

  def y_=(nv: Float) = {
    val sv = Math.min(Math.max(boundingBox.yMin, nv), boundingBox.yMax)
    if (y != sv) {
      point = Point(x, sv, z)
    }
  }

  def z_=(nv: Float) = {
    val sv = Math.min(Math.max(boundingBox.zMin, nv), boundingBox.zMax)
    if (z != sv) {
      point = Point(x, y, sv)
    }
  }

  private def sanitizePoint(): Unit = {
    val sx = Math.min(Math.max(boundingBox.xMin, x), boundingBox.xMax)
    val sy = Math.min(Math.max(boundingBox.yMin, y), boundingBox.yMax)
    val sz = Math.min(Math.max(boundingBox.zMin, z), boundingBox.zMax)
    point = Point(sx, sy, sz)
  }

  private var _boundingBox: BoundingBox = BoundingBox.Invalid

  def boundingBox = _boundingBox

  private def boundingBox_=(nb: BoundingBox): Unit = {
    if (_boundingBox != nb) {
      val wasInvalid = _boundingBox == BoundingBox.Invalid
      _boundingBox = nb
      publishEvent(event.BoundingBoxChanged(this))
      if (wasInvalid) center()
      sanitizePoint()
    }
  }

  def viewports: List[ViewportPanel] = frame.perspectivesPanel.perspectiveInstance.viewports

  private def updateBoundingBox(): Unit = {
    boundingBox = viewports.foldLeft(BoundingBox.Invalid: BoundingBox)({
      case (bb, vp) =>
        bb.union(vp.currentBoundingBox)
    })
  }

  private def perspectiveChanged(): Unit = {
    viewports.foreach(vp => listenTo(vp))
    updateBoundingBox()
  }

  def center(): Unit = {
    point = boundingBox.center
  }

  reactions += {
    case PerspectivesPanel.event.PerspectiveChanged(_) => perspectiveChanged()
    case ViewportPanel.event.BoundingBoxChanged(_) => updateBoundingBox()
    case ViewportPanel.event.Detached(vp) => deafTo(vp)
  }

  def setup(): Unit = {
    listenTo(frame.perspectivesPanel)
    perspectiveChanged()
  }
}
