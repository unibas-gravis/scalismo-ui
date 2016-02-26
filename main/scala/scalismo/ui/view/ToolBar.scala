package scalismo.ui.view

import scala.swing.{Component, Orientable}

class ToolBar extends Component with Orientable.Wrapper {
  override lazy val peer = new javax.swing.JToolBar with SuperMixin

  def floatable: Boolean = peer.isFloatable

  def floatable_=(b: Boolean) = peer.setFloatable(b)

  def rollover: Boolean = peer.isRollover

  def rollover_=(b: Boolean) = peer.setRollover(b)

  // constructor
  floatable = false
  rollover = true
}
