package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.Orientation
import scala.swing.SplitPane

import org.statismo.stk.ui.Workspace

class PropertiesPanel(val workspace: Workspace) extends BorderPanel {
  val scene = new SceneTreePanel(workspace);
  val details = new SceneObjectPropertiesPanel(workspace);

  setupUi

  def setupUi {
    val child = new BorderPanel {
      val split = new SplitPane(Orientation.Horizontal, scene, details) {
        resizeWeight = 0.5
      }
      layout(split) = Center
    }
    layout(child) = Center
  }
}