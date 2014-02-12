package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Workspace
import scala.swing.SplitPane
import scala.swing.Orientation

class WorkspacePanel(val workspace: Workspace) extends BorderPanel {
  lazy val properties = new PropertiesPanel(workspace)
  lazy val viewports = new ViewportsPanel(workspace)

  setupUi
  
  def setupUi = {
    val child = new BorderPanel {
	  layout(properties) = West
	  layout(viewports) = Center
    }
    layout(child) = Center
  }
}