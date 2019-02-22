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

package scalismo.ui.view.action.popup

import java.awt.Color
import java.awt.event.{ MouseAdapter, MouseEvent }

import javax.swing.{ BorderFactory, Icon, JComponent }
import scalismo.ui.control.NodeVisibility
import scalismo.ui.control.NodeVisibility.{ Invisible, PartlyVisible, Visible }
import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }
import scalismo.ui.view.util.ScalableUI.implicits._

import scala.swing._

object ChildVisibilityAction extends PopupAction.Factory {
  override def apply(nodes: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupActionWithOwnMenu] = {
    val affected = allMatch[GroupNode](nodes).filterNot(g => g.renderables.find(_ => true).size == 0)
    if (affected.isEmpty) {
      Nil
    } else {
      List(new ChildVisibilityAction(affected))
    }
  }
}

class ChildVisibilityAction(nodes: List[GroupNode])(implicit frame: ScalismoFrame) extends PopupActionWithOwnMenu {
  val control = frame.sceneControl.nodeVisibility

  override def menuItem: JComponent = {
    val viewports = frame.perspective.viewports
    if (viewports.length > 1) {
      val menu = new Menu("Children visible in") {
        def updateIcon(): Unit = {
          val state = control.getVisibilityState(nodes.flatMap(_.renderables.find(_ => true)), frame.perspective.viewports)
          icon = iconFor(state)
        }

        listenTo(control)

        reactions += {
          case NodeVisibility.event.NodeVisibilityChanged(node, viewport) =>
            if (nodes.contains(node) && viewports.contains(viewport)) {
              updateIcon()
              peer.repaint()
            }
        }

        peer.add(new ViewportVisibilityItem(viewports, "(all)").peer)

        viewports.foreach { vp =>
          peer.add(new ViewportVisibilityItem(List(vp), vp.name).peer)
        }
        updateIcon()
      }
      menu.peer

    } else {
      new ViewportVisibilityItem(viewports, "Children visible").peer
    }
  }

  def iconFor(state: NodeVisibility.State): Icon = {
    val icon = state match {
      case Visible => BundledIcon.Visible
      case Invisible => BundledIcon.Invisible
      case PartlyVisible => BundledIcon.Visible.colored(Color.GRAY)
    }
    icon.standardSized()
  }

  class ViewportVisibilityItem(viewports: List[ViewportPanel], name: String) extends Label(name) {
    val tb = 2.scaled
    val lr = 12.scaled

    def currentState = control.getVisibilityState(nodes.flatMap(_.renderables.find(_ => true)), viewports)

    border = BorderFactory.createEmptyBorder(tb, lr, tb, lr)
    icon = iconFor(currentState)

    peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        if (e.getButton == MouseEvent.BUTTON1) {
          val toggle = currentState match {
            case Visible => false
            case _ => true
          }
          control.setVisibility(nodes.flatMap(_.renderables), viewports, toggle)
        }
      }
    })

    listenTo(control)

    reactions += {
      case NodeVisibility.event.NodeVisibilityChanged(node, viewport) =>
        if (nodes.flatMap(_.renderables).contains(node) && viewports.contains(viewport)) {
          icon = iconFor(currentState)
          peer.repaint()
        }
    }
  }

}
