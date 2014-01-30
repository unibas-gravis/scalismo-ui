package org.statismo.stk.ui.view.swing

import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position._
import org.statismo.stk.ui.Workspace
import scala.swing.Component
import scala.swing.GridPanel

class ViewportsPanel(val workspace: Workspace) extends BorderPanel {
	def onViewportsChanged = {
	  val viewports = workspace.viewports.map(v => new ViewportPanel(v))
	  val inner: Component = {
	    if (viewports.length == 1) viewports.head
	    else {
	      val columns = 2
	      val rows = Math.ceil((viewports.length / columns)).toInt
	      val panel = new GridPanel(rows, columns)
	      panel.contents ++= viewports
	      panel
	    }
	  }
	  layout(inner) = Center
	}
	
	// in constructor, initialize once:
	onViewportsChanged
}