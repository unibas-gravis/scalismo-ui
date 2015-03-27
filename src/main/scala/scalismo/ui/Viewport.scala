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

  // Note: the on*() methods below return a boolean indicating whether
  // the event should be handled by the rendering layer (i.e., VTK).
  // Normally, you would return true, but by returning false you can
  // "swallow" the event, and VTK will never know it happened.

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

  override def onLeftButtonDown(pt: Point) = {
    if (!dragStart.isDefined) {
      TwoDViewport.ImageWindowLevel.dragStart()
      dragStart = Some(pt)
    }
    false
  }

  override def onLeftButtonUp(pt: Point) = {
    if (dragStart.isDefined) {
      TwoDViewport.ImageWindowLevel.dragEnd()
    }
    dragStart = None
    false
  }

  override def onMouseMove(pt: Point): Boolean = {
    dragStart.map { start =>
      val dx = pt.x - start.x
      val dy = pt.y - start.y
      TwoDViewport.ImageWindowLevel.dragUpdate(dx, dy)
      false
    }.getOrElse(true)
  }
}

object TwoDViewport {

  case class ImageWindowLevelChanged(window: Double, level: Double) extends Event

  /**
   * A global singleton containing window/level settings for all 2D volume slices.
   */
  object ImageWindowLevel extends EdtPublisher {
    private var _window: Double = 256
    private var _level: Double = 128

    def window: Double = _window
    def level: Double = _level

    private var dragStartWindow: Option[Double] = None
    private var dragStartLevel: Option[Double] = None

    private[TwoDViewport] def dragStart() = {
      dragStartWindow = Some(_window)
      dragStartLevel = Some(_level)
    }

    private[TwoDViewport] def dragEnd() = {
      dragStartWindow = None
      dragStartLevel = None
    }

    private[TwoDViewport] def dragUpdate(deltaX: Double, deltaY: Double) = {
      (dragStartWindow, dragStartLevel) match {
        case (Some(sw), Some(sl)) =>
          _window = Math.max(0, sw + deltaX)
          _level = Math.max(0, sl + deltaY)

          if (_window != sw || _level != sl) {
            publishEdt(ImageWindowLevelChanged(_window, _level))
          }

        case _ => /* do nothing */
      }
    }
  }
}

