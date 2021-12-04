/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view

import javax.swing
import javax.swing.{JButton, JToolBar}
import scala.swing.{Action, Button, Component, Orientable}

class ToolBar extends Component {
  override lazy val peer: JToolBar = new javax.swing.JToolBar with SuperMixin

  def add(c: Component): Unit = {
    peer.add(c.peer)
    peer.repaint()
    peer.revalidate()
  }

  def remove(c: Component): Unit = {
    peer.remove(c.peer)
    peer.repaint()
    peer.revalidate()
  }

  /**
   * Convenience method to directly add a `scala.swing.Action` to a ToolBar.
   *
   * This method will create a `scala.swing.Button` bound to the specified action,
   * add it to the toolbar, and return that button.
   *
   * @param action the action to be added
   * @return the button object that was added to the toolbar and bound to the action.
   */
  def add(action: Action): Button = {
    val actionPeer: javax.swing.Action = action.peer
    val jb = peer.add(actionPeer)
    new Button {
      override lazy val peer: JButton = jb
    }
  }

  def floatable: Boolean = peer.isFloatable

  def floatable_=(b: Boolean): Unit = peer.setFloatable(b)

  def rollover: Boolean = peer.isRollover

  def rollover_=(b: Boolean): Unit = peer.setRollover(b)

  // constructor
  floatable = false
  rollover = true

}
