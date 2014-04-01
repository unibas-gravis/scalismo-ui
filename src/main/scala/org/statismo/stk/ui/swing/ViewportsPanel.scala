package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.Component
import scala.swing.GridPanel

import org.statismo.stk.ui._

class ViewportsPanel(val workspace: Workspace) extends BorderPanel {
  listenTo(workspace.scene)
  reactions += {
    case Scene.PerspectiveChanged(s) => updateUi(); revalidate()
  }

  updateUi()

  def updateUi() = {
    val child: Component = workspace.scene.perspective match {
      case p: SingleViewportPerspective => singleViewportPanel(p)
      case p: XOnlyPerspective => singleViewportPanel(p)
      case p: YOnlyPerspective => singleViewportPanel(p)
      case p: ZOnlyPerspective => singleViewportPanel(p)
      case p: TwoViewportsPerspective => gridViewportsPanel(p, 1, 2)
      case p: FourViewportsPerspective => gridViewportsPanel(p, 2, 2)
      case p: SlicerAltPerspective => gridViewportsPanel(p, 2, 2)
      case p: SlicerPerspective => slicerPanel(p)
    }
    layout(child) = BorderPanel.Position.Center
  }

  def singleViewportPanel(perspective: Perspective) = ViewportPanel(workspace, perspective.viewports.head)

  def gridViewportsPanel(perspective: Perspective, rows: Int, columns: Int) = {
    val panel = new GridPanel(rows, columns)
    perspective.viewports.foreach {
      v => panel.contents += ViewportPanel(workspace, v)
    }
    panel
  }

  def slicerPanel(perspective: Perspective) = {
    val upper = ViewportPanel(workspace, perspective.viewports.head)
    val lower = new GridPanel(1, 3)
    perspective.viewports.drop(1).foreach {
      v => lower.contents += ViewportPanel(workspace, v)
    }
    new BorderPanel {
      layout(lower) = BorderPanel.Position.South
      layout(upper) = BorderPanel.Position.Center
    }
  }
}