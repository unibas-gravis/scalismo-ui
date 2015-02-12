package scalismo.ui.swing

import scalismo.ui.Workspace

import scala.swing.{ BorderPanel, Component }

class TunableWorkspacePanel(workspace: Workspace) extends WorkspacePanel(workspace) {

  def addAfterInit(c: Component, l: Constraints) {
    val old = child.layoutManager.getLayoutComponent(l.toString)
    if (old != null) child.peer.remove(old)
    child.peer.add(c.peer, l.toString)
  }

  def remove(l: Constraints) {
    val old = child.layoutManager.getLayoutComponent(l.toString)
    if (old != null) {
      child.peer.remove(old)
    }
  }
}

class WorkspacePanel(val workspace: Workspace) extends BorderPanel {
  lazy val toolbar: ScalismoToolbar = new ScalismoToolbar(workspace)
  lazy val properties = new PropertiesPanel(workspace)
  lazy val perspectives = new PerspectivesPanel(workspace)
  //  lazy val console = new ConsolePanel

  val child = setupUi()

  def setupUi() = {
    val child = new BorderPanel {
      layout(toolbar) = BorderPanel.Position.North
      layout(properties) = BorderPanel.Position.West
      layout(perspectives) = BorderPanel.Position.Center
      //layout(console) = BorderPanel.Position.East
    }
    layout(child) = BorderPanel.Position.Center
    child
  }
}