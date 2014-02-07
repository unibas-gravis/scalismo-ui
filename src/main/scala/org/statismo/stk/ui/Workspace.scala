package org.statismo.stk.ui

import scala.swing.event.Event
import scala.swing.Publisher

object Workspace {
  case object SelectedObjectChanged extends Event
}

class Workspace(val scene: Scene) extends Publisher {
  private var _viewports: Seq[Viewport] = Seq(new Viewport(this))
  def viewports = { _viewports }
  
  private var _landmarkClickMode = false
  def landmarkClickMode = _landmarkClickMode

  private var _selectedObject: Option[SceneTreeObject] = None
  def selectedObject = { _selectedObject }
  def selectedObject_=(newObject: Option[SceneTreeObject]) = {
    if (!(_selectedObject eq newObject)) {
      _selectedObject = newObject
      if (newObject.isDefined) {
        if (newObject.get.isInstanceOf[DisplayableLandmarks]) {
          if (!_landmarkClickMode) {
            _landmarkClickMode = true
          }
        } else {
          if (_landmarkClickMode) {
            _landmarkClickMode = false
          }
        }
      }
      publish(Workspace.SelectedObjectChanged)
    }
  }
}