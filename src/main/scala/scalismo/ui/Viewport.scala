package scalismo.ui

import java.awt.Point
import java.io.File

import scala.swing.event.Event

object Viewport {

  case class Destroyed(source: Viewport) extends Event

  case class BoundingBoxChanged(source: Viewport) extends Event

  case class ResetCameraRequest(source: Viewport) extends Event

  case class ScrollRequest(source: Viewport, delta: Int) extends Event

  case class ScreenshotRequest(source: Viewport, outputFile: File) extends Event

}

trait Viewport extends Nameable {
  def scene: Scene

  def destroy(): Unit = publishEdt(Viewport.Destroyed(this))

  def resetCamera(): Unit = publishEdt(Viewport.ResetCameraRequest(this))

  def screenshot(outputFile: File): Unit = publishEdt(Viewport.ScreenshotRequest(this, outputFile))

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

  def scroll(delta: Int): Unit = publishEdt(Viewport.ScrollRequest(this, delta))
}

class ThreeDViewport(override val scene: Scene, name: Option[String] = None) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))

  override def scroll(delta: Int): Unit = {} // no need to publish anything, it's not handled anyway

}

class TwoDViewport(override val scene: Scene, val axis: Axis.Value, name: Option[String] = None) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))

  private var dragStart: Option[Point] = None

  override def onLeftButtonDown(pt: Point) = {
    if (dragStart.isEmpty) {
      scene.imageWindowLevel.dragStart()
      dragStart = Some(pt)
    }
    false
  }

  override def onLeftButtonUp(pt: Point) = {
    if (dragStart.isDefined) {
      scene.imageWindowLevel.dragEnd()
    }
    dragStart = None
    false
  }

  override def onMouseMove(pt: Point): Boolean = {
    dragStart.map { start =>
      val dx = pt.x - start.x
      val dy = pt.y - start.y
      scene.imageWindowLevel.dragUpdate(dx, dy)
      false
    }.getOrElse(true)
  }
}
