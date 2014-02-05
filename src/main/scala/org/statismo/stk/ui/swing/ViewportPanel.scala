package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.vtk.VtkPanel
import javax.swing.border.TitledBorder

class ViewportPanel(val viewport: Viewport) extends BorderPanel {
  
  border = new TitledBorder(null, "3D View", TitledBorder.LEADING, 0, null, null)
	val vtk = new VtkPanel(viewport)
	layout(vtk) = Center
}