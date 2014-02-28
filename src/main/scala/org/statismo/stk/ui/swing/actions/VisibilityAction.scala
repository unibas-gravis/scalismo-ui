package org.statismo.stk.ui.swing.actions

import java.awt.event.ItemEvent
import java.awt.event.ItemListener

import scala.swing.CheckMenuItem
import scala.swing.Menu
import scala.swing.event.ButtonClicked

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.Viewport

import javax.swing.JCheckBox

class VisibilityAction extends SceneTreePopupAction("Visible in...") {
  private class VCheckBox(context: SceneTreeObject, viewport: Viewport) extends JCheckBox(viewport.name) with ItemListener {
    setSelected(context.isShownInViewport(viewport))
    def itemStateChanged(event: ItemEvent) = {
      if (isSelected()) {
        context.showInViewport(viewport)
      } else {
        context.hideInViewport(viewport)
      }
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
        selected = obj.isShownInViewport(viewports.head)
        reactions += {
          case ButtonClicked(b) =>
            if (selected) {
              obj.showInViewport(viewports.head)
            } else {
              obj.hideInViewport(viewports.head)
            }
        }
      }
      Some(item)
    } else {
      val item = new Menu(this.title) {
        viewports foreach { v =>
          peer.add(new VCheckBox(obj, v))
        }
      }
      Some(item)
    }
  }
} 