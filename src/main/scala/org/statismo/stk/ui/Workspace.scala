package org.statismo.stk.ui

import scala.swing.event.Event

object Workspace {

  case object SelectedObjectChanged extends Event

  // this is a hack
  case object PleaseLayoutAgain extends Event
}

class Workspace(val scene: Scene) extends EdtPublisher {
  def viewports = scene.viewports

  private var _landmarkClickMode = false

  def landmarkClickMode = _landmarkClickMode

  def landmarkClickMode_=(b: Boolean) {
    _landmarkClickMode = b
  }

  private var _selectedObject: Option[SceneTreeObject] = None

  def selectedObject = {
    _selectedObject
  }

  def selectedObject_=(newObject: Option[SceneTreeObject]) = {
    if (!(_selectedObject eq newObject)) {
      _selectedObject = newObject
      publishEdt(Workspace.SelectedObjectChanged)
    }
  }
}