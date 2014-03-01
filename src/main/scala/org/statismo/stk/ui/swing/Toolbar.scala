package org.statismo.stk.ui.swing

import scala.swing.Action
import scala.swing.Button
import scala.swing.Component
import scala.swing.Orientable

class Toolbar extends Component with Orientable.Wrapper {
  override lazy val peer = new javax.swing.JToolBar with SuperMixin

  def add(action: Action): Button = {
    val jb = peer.add(action.peer)
    new Button {
      override lazy val peer = jb
    }
  }

  def add(c: Component) = {
    peer.add(c.peer)
  }

  def floatable: Boolean = peer.isFloatable

  def floatable_=(b: Boolean) = peer.setFloatable(b)

  def rollover: Boolean = peer.isRollover

  def rollover_=(b: Boolean) = peer.setRollover(b)

}