package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import org.statismo.stk.ui.Workspace
import org.statismo.stk.ui.StatismoFrame

class WorkspacePanel(val workspace: Workspace) extends BorderPanel {
  lazy val toolbar: StatismoToolbar = new StatismoToolbar(workspace)
  lazy val properties = new PropertiesPanel(workspace)
  lazy val viewports = new ViewportsPanel(workspace)
//  lazy val console = new ConsolePanel

  setupUi()

  def setupUi() = {
    val child = new BorderPanel {
      layout(toolbar) = BorderPanel.Position.North
      layout(properties) = BorderPanel.Position.West
      layout(viewports) = BorderPanel.Position.Center
      //layout(console) = BorderPanel.Position.East
    }
    layout(child) = BorderPanel.Position.Center
  }
}