package org.statismo.stk.ui.swing.util

import java.io.File
import javax.swing.JFileChooser
import java.awt.{Component => AComponent}
import javax.swing.JDialog
import javax.swing.JLabel
import java.awt.BorderLayout
import scala.swing.Component
import scala.swing.BorderPanel
import scala.swing.Label

class EnhancedFileChooser(dir:File)  extends scala.swing.FileChooser(dir) {
  override lazy val peer = new EnhancedJFileChooser()
  def this() = this(null)
}

class EnhancedJFileChooser extends JFileChooser {
  override def createDialog(parent: AComponent): JDialog = {
    val dialog = super.createDialog(parent)
    decorateDialog(dialog)
  }
  def leftComponent: Option[Component] = {
    val panel = new BorderPanel {
      val title = new Label("TODO")
      val nothing = new Label("TODO")
      layout(title) = BorderPanel.Position.North
      layout(nothing) = BorderPanel.Position.Center
    }
    Some(panel)
  }
  
  def decorateDialog(dialog: JDialog) = {
    val cp = dialog.getContentPane()
    leftComponent match {
      case Some(component) => cp.add(component.peer, BorderLayout.WEST)
      case None =>
    }
    dialog
  }
  
  
}
