package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Workspace
import scala.swing.SplitPane
import scala.swing.Orientation

class WorkspacePanel(val workspace: Workspace) extends BorderPanel { //with SceneAdapter {
  lazy val properties = new PropertiesPanel(workspace)
  lazy val viewports = new ViewportsPanel(workspace)

  // FIXME
//  val split = new SplitPane(Orientation.Vertical, properties, viewports)
//  layout(split) = Center
  layout(properties) = West
  layout(viewports) = Center

}