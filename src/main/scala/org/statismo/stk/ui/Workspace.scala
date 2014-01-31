package org.statismo.stk.ui

import scala.swing.event.Event
import scala.swing.Publisher

object Workspace {
  case object SelectedObjectChanged extends Event
}

class Workspace(val scene: Scene) extends Publisher {
  private var _viewports: Seq[Viewport] = Seq(new Viewport(scene), new Viewport(scene), new Viewport(scene), new Viewport(scene))
  def viewports = { _viewports }

  private var _selectedObject: Option[SceneObject] = None
  def selectedObject = { _selectedObject }
  def selectedObject_=(newObject: Option[SceneObject]) = {
    if (_selectedObject != newObject) {
      _selectedObject = newObject
      publish(Workspace.SelectedObjectChanged)
    }
  }
}