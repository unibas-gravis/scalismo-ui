package scalismo.ui.control

import scalismo.geometry.{ Point, Point3D }
import scalismo.ui.control.SlicingPosition.event
import scalismo.ui.control.SlicingPosition.renderable.BoundingBoxRenderable
import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.{ BoundingBox, Renderable, Scene }
import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view._

object SlicingPosition {

  object event {

    case class VisibilityChanged(source: SlicingPosition) extends Event

    case class Intersections2DVisibilityChanged(source: SlicingPosition) extends Event

    case class SliceOpacity3DChanged(source: SlicingPosition) extends Event

    case class PointChanged(source: SlicingPosition, previous: Point3D, current: Point3D) extends Event

    case class BoundingBoxChanged(source: SlicingPosition) extends Event

  }

  object renderable {

    class BoundingBoxRenderable(val source: SlicingPosition) extends Renderable

  }

}

class SlicingPosition(val scene: Scene, val frame: ScalismoFrame) extends ScalismoPublisher {

  private var _visible = GlobalSettings.get[Boolean](GlobalSettings.Keys.SlicingPositionShow).getOrElse(false)

  def visible = _visible

  def visible_=(newVisible: Boolean) = {
    if (_visible != newVisible) {
      _visible = newVisible
      GlobalSettings.set(GlobalSettings.Keys.SlicingPositionShow, newVisible)
      publishEvent(event.VisibilityChanged(this))
      //scene.publishEdt(Scene.SlicingPosition.SlicesVisibleChanged(this))
      //scene.publishEdt(Scene.VisibilityChanged(scene))
    }
  }

  private var _intersectionsVisible2D = GlobalSettings.get[Boolean](GlobalSettings.Keys.SlicingPositionShowIntersections2D).getOrElse(false)

  def intersectionsVisible2D = _intersectionsVisible2D

  def intersectionsVisible2D_=(nv: Boolean) = {
    if (_intersectionsVisible2D != nv) {
      _intersectionsVisible2D = nv
      GlobalSettings.set(GlobalSettings.Keys.SlicingPositionShowIntersections2D, nv)
      publishEvent(event.Intersections2DVisibilityChanged(this))
      //scene.publishEdt(Scene.SlicingPosition.IntersectionsVisibleChanged(this))
      //scene.publishEdt(Scene.VisibilityChanged(scene))
    }
  }

  private var _sliceOpacity3D = Math.max(0.0, Math.min(1.0, GlobalSettings.get[Double](GlobalSettings.Keys.SlicingPositionSliceOpacity3D).getOrElse(0.0)))

  def opacity = _sliceOpacity3D

  def opacity_=(nv: Double) = {
    val sane = Math.max(0.0, Math.min(1.0, nv))
    if (_sliceOpacity3D != sane) {
      _sliceOpacity3D = sane
      GlobalSettings.set(GlobalSettings.Keys.SlicingPositionSliceOpacity3D, sane)
      publishEvent(event.SliceOpacity3DChanged(this))
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
      publishEvent(event.PointChanged(this, prev, np))
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

  def viewports: List[ViewportPanel] = frame.perspectivesPanel.viewports

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
    case PerspectivesPanel.event.PerspectiveChanged(_, _, _) => perspectiveChanged()
    case ViewportPanel.event.BoundingBoxChanged(_) => updateBoundingBox()
    case ViewportPanel.event.Detached(vp) => deafTo(vp)
  }

  def initialize(): Unit = {
    listenTo(frame.perspectivesPanel)
    perspectiveChanged()
  }

  // renderables
  private lazy val boundingBoxRenderable = new BoundingBoxRenderable(this)

  def renderablesFor(viewport: ViewportPanel): List[Renderable] = {
    viewport match {
      case _3d: ViewportPanel3D => List(boundingBoxRenderable)
      case _2d: ViewportPanel2D => List(boundingBoxRenderable)
    }
  }
}
