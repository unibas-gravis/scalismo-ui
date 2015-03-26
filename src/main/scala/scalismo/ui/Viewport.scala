package scalismo.ui

import java.awt.Point

import scala.swing.event.Event

object Viewport {

  case class Destroyed(source: Viewport) extends Event

  case class BoundingBoxChanged(source: Viewport) extends Event

  case class InitialCameraChange(pitch: Option[Double], roll: Option[Double], yaw: Option[Double])

  val NoInitialCameraChange = InitialCameraChange(None, None, None)

  val ThreeDViewportClassName = "scalismo.ui.ThreeDViewport"
  val TwoDViewportClassName = "scalismo.ui.TwoDViewport"
}

trait Viewport extends Nameable {
  def scene: Scene

  def destroy(): Unit = {
    publishEdt(Viewport.Destroyed(this))
  }

  def onLeftButtonDown(pt: Point): Boolean = true

  def onLeftButtonUp(pt: Point): Boolean = true

  def onMiddleButtonDown(pt: Point): Boolean = true

  def onMiddleButtonUp(pt: Point): Boolean = true

  def onRightButtonDown(pt: Point): Boolean = true

  def onRightButtonUp(pt: Point): Boolean = true

  def onMouseMove(pt: Point): Boolean = true

  private var _currentBoundingBox = BoundingBox.None

  def currentBoundingBox = _currentBoundingBox

  private[ui] def currentBoundingBox_=(nb: BoundingBox) = {
    if (currentBoundingBox != nb) {
      _currentBoundingBox = nb
      publishEdt(Viewport.BoundingBoxChanged(this))
    }
  }

  def initialCameraChange = Viewport.NoInitialCameraChange
}

class ThreeDViewport(override val scene: Scene, name: Option[String] = None) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
}

class TwoDViewport(override val scene: Scene, val axis: Axis.Value, name: Option[String] = None) extends Viewport {
  import TwoDViewport._
  name_=(name.getOrElse(Nameable.NoName))

  override lazy val initialCameraChange = axis match {
    case Axis.Z => Viewport.NoInitialCameraChange
    case Axis.Y => Viewport.InitialCameraChange(Some(90), None, None)
    case Axis.X => Viewport.InitialCameraChange(None, None, Some(90))
  }

  private var dragStart: Option[Point] = None
  override def onLeftButtonUp(pt: Point) = {
    if (dragStart.isDefined) {
      TwoDViewport.publishEdt(DragEndEvent)
    }
    dragStart = None
    false
  }

  override def onLeftButtonDown(pt: Point) = {
    if (!dragStart.isDefined) {
      println("start dragging: " + pt)
      TwoDViewport.publishEdt(DragStartEvent)
      dragStart = Some(pt)
    }
    false
  }

  override def onMouseMove(pt: Point): Boolean = {
    dragStart.map { start =>
      val dx = pt.x - start.x
      val dy = pt.y - start.y
      TwoDViewport.publishEdt(DragUpdateEvent(dx, dy))
    }
    false
  }
}

object TwoDViewport extends EdtPublisher {
  case object DragStartEvent extends Event
  case object DragEndEvent extends Event
  case class DragUpdateEvent(deltaX: Int, deltaY: Int) extends Event
}

