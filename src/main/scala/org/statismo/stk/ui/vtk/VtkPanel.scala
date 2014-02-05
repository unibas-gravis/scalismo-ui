package org.statismo.stk.ui.vtk

import java.awt.BorderLayout

import scala.swing.Component
import scala.swing.Reactor

import org.statismo.stk.ui.Viewport

import javax.swing.JPanel

class VtkPanel(val viewport: Viewport) extends Component with Reactor {
  lazy val ui = new VtkCanvas
  override lazy val peer = {
    val panel = new JPanel(new BorderLayout())
    panel.add(ui, BorderLayout.CENTER);
    panel
  }
  lazy val vtk = new VtkViewport(viewport, ui.GetRenderer())
  listenTo(vtk)
  
  reactions += {
    case VtkContext.RenderRequest(s) => {
      ui.Render()
    }
  }
  
}