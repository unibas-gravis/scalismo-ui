package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.vtk.VtkPanel
import javax.swing.border.TitledBorder
import org.statismo.stk.ui.Nameable
import org.statismo.stk.ui.Workspace

class ViewportPanel(val workspace: Workspace, val viewport: Viewport) extends BorderPanel {

  val title = new TitledBorder(null, viewport.name, TitledBorder.LEADING, 0, null, null)
  border = title
  listenTo(viewport)
  val vtk = new VtkPanel(workspace, viewport)
  layout(vtk) = Center
  
  reactions += {
    case Nameable.NameChanged(v) => {
      if (v eq viewport) {
        title.setTitle(viewport.name)
        revalidate
      }
    }
  }
}