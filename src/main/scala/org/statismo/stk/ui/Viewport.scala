package org.statismo.stk.ui

import scala.swing.event.Event

object Viewport {
  case class Destroyed(source: Viewport) extends Event
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

  def initialCameraChange = Viewport.NoInitialCameraChange
}

class ThreeDViewport(override val scene: Scene, name: Option[String]) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
  override val isMouseSensitive = true
}

class TwoDViewport(override val scene: Scene, val axis: ThreeDImageAxis.Value, name: Option[String]) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
  override val isMouseSensitive = false

  override lazy val initialCameraChange = axis match {
    case ThreeDImageAxis.Z => Viewport.NoInitialCameraChange
    case ThreeDImageAxis.Y => Viewport.InitialCameraChange(Some(90), None, None)
    case ThreeDImageAxis.X => Viewport.InitialCameraChange(None, None, Some(90))
  }
}

