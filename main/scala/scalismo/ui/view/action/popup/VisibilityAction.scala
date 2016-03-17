package scalismo.ui.view.action.popup

import java.awt.Color
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ BorderFactory, Icon }

import scalismo.ui.control.NodeVisibility
import scalismo.ui.control.NodeVisibility.State
import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.RenderableSceneNode
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }

import scala.swing._

object VisibilityAction extends PopupAction.Factory {
  override def apply(nodes: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupActionWithOwnMenu] = {
    val affected = allMatch[RenderableSceneNode](nodes)
    if (affected.isEmpty) {
      Nil
    } else {
      List(new VisibilityAction(affected))
    }
  }
}

class VisibilityAction(nodes: List[RenderableSceneNode])(implicit frame: ScalismoFrame) extends PopupActionWithOwnMenu {
  override def menuItem: MenuItem = {
    val menu = new Menu("Visible in")
    frame.perspective.viewports.foreach { vp =>
      menu.peer.add(new ViewportVisibilityItem(vp).peer)
    }
    menu
  }

  def iconFor(state: NodeVisibility.State): Icon = {
    val icon = state match {
      case State.Visible => BundledIcon.Visible
      case State.Invisible => BundledIcon.Invisible
      case State.PartlyVisible => BundledIcon.Visible.colored(Color.GRAY)
    }
    icon.standardSized()
  }

  class ViewportVisibilityItem(viewport: ViewportPanel) extends BorderPanel {
    val b = 10
    border = BorderFactory.createEmptyBorder(b, b, b, b)
    layoutManager.setHgap(b)
    val label = new Label(viewport.name)

    val control = frame.sceneControl.nodeVisibility

    def currentState = control.getVisibilityState(nodes, viewport)

    val iconLabel = new Label {
      icon = iconFor(currentState)
      peer.addMouseListener(new MouseAdapter {
        override def mouseClicked(e: MouseEvent): Unit = {
          // just in case
          if (e.getButton == MouseEvent.BUTTON1) {
            //println(s"bef $viewport $nodes $currentState")
            val toggle = currentState match {
              case NodeVisibility.State.Visible => false
              case _ => true
            }
            control.setVisibility(nodes, viewport, toggle)
            //println(s"aft $viewport $nodes $currentState")
            icon = iconFor(currentState)

            // not sure which of these are required... we do the full monty
            peer.invalidate()
            peer.revalidate()
            peer.validate()
            peer.repaint()
          }

        }
      })
    }
    layout(label) = BorderPanel.Position.Center
    layout(iconLabel) = BorderPanel.Position.West

  }

}
