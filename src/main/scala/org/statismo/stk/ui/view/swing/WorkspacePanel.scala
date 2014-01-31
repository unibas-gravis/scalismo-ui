package org.statismo.stk.ui.view.swing

import org.statismo.stk.ui.SceneAdapter
import java.awt.BorderLayout
import scala.swing.BorderPanel.Position._
import scala.swing.BorderPanel
import org.statismo.stk.ui.Scene
import org.statismo.stk.ui.SceneChanged
import org.statismo.stk.ui.Workspace

class WorkspacePanel(val workspace: Workspace) extends BorderPanel { //with SceneAdapter {
  layout(new PropertiesPanel(workspace)) = West
  layout(new ViewportsPanel(workspace)) = Center
  
}