package org.statismo.stk.ui

import scala.swing.event.Event

object Viewport {

  case class Destroyed(source: Viewport) extends Event

  case class InitialCameraChange(pitch: Option[Double], roll: Option[Double], yaw: Option[Double])

  val NoInitialCameraChange = InitialCameraChange(None, None, None)
}

trait Viewport extends Nameable {
  def scene: Scene

  def destroy(): Unit = {
    publish(Viewport.Destroyed(this))
  }

  def isMouseSensitive: Boolean

  def supportsShowingObject(target: SceneTreeObject): Boolean = {
    target match {
      case d: Displayable => supportsShowingDisplayable(d)
      case _ => true
    }
  }

  protected def supportsShowingDisplayable(target: Displayable): Boolean = true

  def initialCameraChange = Viewport.NoInitialCameraChange
}

class ThreeDViewport(override val scene: Scene, name: Option[String]) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
  override val isMouseSensitive = true
}

class SliceViewport(override val scene: Scene, val axis: ThreeDImageAxis.Value, name: Option[String]) extends Viewport {
  name_=(name.getOrElse(Nameable.NoName))
  override val isMouseSensitive = false

  override def supportsShowingDisplayable(target: Displayable): Boolean = {
    target match {
      case plane: ThreeDImagePlane[_] => plane.axis == this.axis
      case _: DisplayableLandmark => true
      case _ => false
    }
  }

  override lazy val initialCameraChange = axis match {
    case ThreeDImageAxis.Z => Viewport.NoInitialCameraChange
    case ThreeDImageAxis.Y => Viewport.InitialCameraChange(Some(90), None, None)
    case ThreeDImageAxis.X => Viewport.InitialCameraChange(None, None, Some(90))
  }
}

