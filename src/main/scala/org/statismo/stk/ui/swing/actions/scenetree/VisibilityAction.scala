package org.statismo.stk.ui.swing.actions.scenetree

import java.awt.event.ItemEvent
import java.awt.event.ItemListener

import scala.swing.CheckMenuItem
import scala.swing.Menu
import scala.swing.event.ButtonClicked

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.Viewport
import scala.language.reflectiveCalls

import javax.swing.JCheckBox

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