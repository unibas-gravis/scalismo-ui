package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.West
import org.statismo.stk.ui.Workspace
import org.statismo.stk.ui.StatismoApp
import org.statismo.stk.ui.StatismoFrame

class WorkspacePanel(val workspace: Workspace)(implicit frame: StatismoFrame) extends BorderPanel {
  lazy val properties = new PropertiesPanel(workspace)
  lazy val viewports = new ViewportsPanel(workspace)
  lazy val console = new ConsolePanel

  setupUi

  def setupUi = {
    val child = new BorderPanel {
      layout(properties) = West
      layout(viewports) = Center
      //layout(console) = BorderPanel.Position.East
    }
    layout(child) = Center
  }
}