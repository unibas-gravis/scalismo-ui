package scalismo.ui.swing.actions.scenetree

import java.awt.event.{ ItemEvent, ItemListener }
import javax.swing.JCheckBox

import scalismo.ui.{ SceneTreeObject, Viewport }

import scala.swing.{ CheckMenuItem, Menu }
import scala.swing.event.ButtonClicked

class VisibilityAction extends SceneTreePopupAction("Visible in...") {

  private class VCheckBox(context: SceneTreeObject, viewport: Viewport) extends JCheckBox(viewport.name) with ItemListener {
    setSelected(context.visible(viewport))

    def itemStateChanged(event: ItemEvent) = {
      context.visible(viewport) = isSelected
    }

    addItemListener(this)
  }

  def isContextSupported(context: Option[SceneTreeObject]) = {
    if (context.isDefined) {
      if (hasSingleViewport(context.get)) {
        title = "Visible"
      } else {
        title = "Visible in"
      }
      true
    } else false
  }

  def hasSingleViewport(context: SceneTreeObject) = {
    context.scene.viewports.length == 1
  }

  override def createMenuItem(context: Option[SceneTreeObject]) = {
    val obj = context.get
    val viewports = obj.scene.viewports
    if (hasSingleViewport(obj)) {
      val item = new CheckMenuItem(title) {
        selected = obj.visible(viewports.head)
        reactions += {
          case ButtonClicked(b) =>
            context.get.visible(viewports.head) = selected
        }
      }
      Some(item)
    } else {
      val item = new Menu(this.title) {
        viewports foreach {
          v =>
            peer.add(new VCheckBox(obj, v))
        }
      }
      Some(item)
    }
  }
}