package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Workspace
import scala.swing.Component
import scala.swing.GridPanel
import org.statismo.stk.ui.Scene
import org.statismo.stk.ui.SingleViewportPerspective
import org.statismo.stk.ui.TwoViewportsPerspective
import org.statismo.stk.ui.Perspective
import org.statismo.stk.ui.FourViewportsPerspective
import org.statismo.stk.ui.SlicerPerspective

class ViewportsPanel(val workspace: Workspace) extends BorderPanel {
  listenTo(workspace.scene)
  reactions += {
    case Scene.PerspectiveChanged(s) => {updateUi(); revalidate}
  }
  
  updateUi()
  
  def updateUi() = {
    val child: Component = workspace.scene.perspective match {
      case _:SingleViewportPerspective => singleViewportPanel(workspace.scene.perspective)
      case _:TwoViewportsPerspective => gridViewportsPanel(workspace.scene.perspective, 1, 2)
      case _:FourViewportsPerspective => gridViewportsPanel(workspace.scene.perspective, 2, 2)
      case _:SlicerPerspective => slicerPanel(workspace.scene.perspective)
    }
    layout(child) = BorderPanel.Position.Center
  }
  
  def singleViewportPanel(perspective: Perspective) = ViewportPanel(workspace, perspective.viewports.head)
  
  def gridViewportsPanel(perspective: Perspective, rows: Int, columns: Int) = {
    val panel = new GridPanel(rows,columns)
    perspective.viewports.foreach { v => panel.contents += ViewportPanel(workspace, v)}
    panel
  }
  
  def slicerPanel(perspective: Perspective) = {
    val upper = ViewportPanel(workspace, perspective.viewports.head)
    val lower = new GridPanel(1, 3)
    perspective.viewports.drop(1).foreach { v => lower.contents += ViewportPanel(workspace, v)}
    new BorderPanel {
      layout(lower) = BorderPanel.Position.South
      layout(upper) = BorderPanel.Position.Center
    }
  }
}