package scalismo.ui.view

import scala.swing.{ Action, Button, Component, Orientable }

class ToolBar extends Component with Orientable.Wrapper {
  override lazy val peer = new javax.swing.JToolBar with SuperMixin

  def add(c: Component) = {
    peer.add(c.peer)
    peer.repaint()
    peer.revalidate()
  }

  def remove(c: Component) = {
    peer.remove(c.peer)
    peer.repaint()
    peer.revalidate()
  }

  /**
   * Convenience method to directly add an [[Action]] to a ToolBar.
   *
   * This method will create a [[Button]] bound to the specified action,
   * add it to the toolbar, and return that button.
   *
   * @param action the action to be added
   * @return the button object that was added to the toolbar and bound to the action.
   */
  def add(action: Action): Button = {
    val jb = peer.add(action.peer)
    new Button {
      override lazy val peer = jb
    }
  }

  def floatable: Boolean = peer.isFloatable

  def floatable_=(b: Boolean) = peer.setFloatable(b)

  def rollover: Boolean = peer.isRollover

  def rollover_=(b: Boolean) = peer.setRollover(b)

  // constructor
  floatable = false
  rollover = true
}
