package org.statismo.stk.ui

import scala.swing.event.Event

object Viewport {
  case class Destroyed(source: Viewport) extends Event
  case class BoundingBoxChanged(source: Viewport) extends Event
  case class InitialCameraChange(pitch: Option[Double], roll: Option[Double], yaw: Option[Double])
  val NoInitialCameraChange = InitialCameraChange(None, None, None)

  val ThreeDViewportClassName = "org.statismo.stk.ui.ThreeDViewport"
  val TwoDViewportClassName = "org.statismo.stk.ui.TwoDViewport"
}

trait Viewport extends Nameable {
  def scene: Scene

  def destroy(): Unit = {
    publish(Viewport.Destroyed(this))
  }

  def isMouseSensitive: Boolean

  private var _currentBoundingBox = BoundingBox.None
  def currentBoundingBox = _currentBoundingBox
  private [ui] def currentBoundingBox_=(nb: BoundingBox) = {
    if (currentBoundingBox != nb) {
      _currentBoundingBox = nb
    }
    publish(Viewport.BoundingBoxChanged(this))
  }

  def initialCameraChange = Viewport.NoInitialCameraChange
}

class ThreeDViewport(override val scene: Scene, name: Option[String] = None) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
  override val isMouseSensitive = true
}

class TwoDViewport(override val scene: Scene, val axis: Axis.Value, name: Option[String] = None) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
  override val isMouseSensitive = false

  override lazy val initialCameraChange = axis match {
    case Axis.Z => Viewport.NoInitialCameraChange
    case Axis.Y => Viewport.InitialCameraChange(Some(90), None, None)
    case Axis.X => Viewport.InitialCameraChange(None, None, Some(90))
  }
}

