package org.statismo.stk.ui

import scala.swing.event.Event

object Viewport {
  case class Destroyed(source: Viewport) extends Event
}

trait Viewport extends Nameable {
  def scene: Scene
  def destroy(): Unit = {
    publish(Viewport.Destroyed(this))
  }
}

class ThreeDViewport(override val scene: Scene, initialName: Option[String]) extends Viewport {
  if (initialName.isDefined) name = initialName.get
}

