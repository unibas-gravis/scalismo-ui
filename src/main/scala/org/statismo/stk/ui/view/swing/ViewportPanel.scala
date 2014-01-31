package org.statismo.stk.ui.view.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.ViewportAdapter
import scala.swing.Button
import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.vtk.VtkPanel
import scala.swing.Label
import javax.swing.border.TitledBorder

class ViewportPanel(val viewport: Viewport) extends BorderPanel with ViewportAdapter {
  
  border = new TitledBorder(null, "3D View", TitledBorder.LEADING, 0, null, null)
	val vtk = new VtkPanel
	layout(vtk) = Center
	def reloadViewport = {
	  vtk.setObjects(viewport.scene.objects)
	}
}