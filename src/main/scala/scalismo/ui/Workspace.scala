package scalismo.ui

import scalismo.ui.Workspace.LandmarkClickModeChanged

import scala.swing.event.Event

object Workspace {

  case class SelectedObjectChanged protected[Workspace] (workspace: Workspace) extends Event

  // this is a hack
  case class PleaseLayoutAgain protected[Workspace] (workspace: Workspace) extends Event

  case class LandmarkClickModeChanged protected[Workspace] (workspace: Workspace, clickMode: Boolean) extends Event
}

class Workspace(val scene: Scene) extends EdtPublisher {
  def viewports = scene.viewports

  private var _landmarkClickMode = false

  def landmarkClickMode = _landmarkClickMode

  def landmarkClickMode_=(b: Boolean): Unit = {
    _landmarkClickMode = b
    publishEdt(LandmarkClickModeChanged(this, b))
  }

  private var _selectedObject: Option[SceneTreeObject] = None

  def selectedObject = {
    _selectedObject
  }

  def selectedObject_=(newObject: Option[SceneTreeObject]) = {
    if (!(_selectedObject eq newObject)) {
      _selectedObject = newObject
      publishEdt(Workspace.SelectedObjectChanged(this))
    }
  }

  def publishPleaseLayoutAgain() = {
    publishEdt(Workspace.PleaseLayoutAgain(this))
  }
}
