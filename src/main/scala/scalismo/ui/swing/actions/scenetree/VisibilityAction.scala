package scalismo.ui.swing.actions.scenetree

import java.awt.event.{ ItemEvent, ItemListener }
import javax.swing.{ JCheckBox, JComponent }

import scalismo.ui.{ Scene, SceneTreeObject, Viewport }

import scala.swing._
import scala.swing.event.ButtonClicked

class VisibilityAction extends SceneTreePopupAction("Visible in...") {

  private class VCheckBox(context: SceneTreeObject, viewport: Viewport) extends JCheckBox(viewport.name) with ItemListener with Reactor {
    setSelected(context.viewportVisibility(viewport))

    addItemListener(this)
    listenTo(context.scene)

    // events from the checkbox itself
    def itemStateChanged(event: ItemEvent) = {
      context.viewportVisibility(viewport) = isSelected
    }

    // events from the scene
    reactions += {
      case Scene.VisibilityChanged(_) =>
        removeItemListener(this)
        setSelected(context.viewportVisibility(viewport))
        addItemListener(this)
    }
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

  def createGlobalActionComponent(ctx: SceneTreeObject, name: String, change: Boolean => Boolean): JComponent = {
    def action(): Action = {
      new Action(name) {
        override def apply(): Unit = {
          ctx.scene.viewports foreach { vp =>
            val ov = ctx.viewportVisibility(vp)
            ctx.viewportVisibility.update(vp, change(ov))
          }
        }
      }
    }

    new Button(action()).peer
  }

  override def createMenuItem(context: Option[SceneTreeObject]) = {
    val obj = context.get
    val viewports = obj.scene.viewports
    if (hasSingleViewport(obj)) {
      val item = new CheckMenuItem(title) {
        selected = obj.viewportVisibility(viewports.head)
        reactions += {
          case ButtonClicked(b) =>
            context.get.viewportVisibility(viewports.head) = selected
        }
      }
      Some(item)
    } else {
      val item = new Menu(this.title) {
        viewports foreach { v =>
          peer.add(new VCheckBox(obj, v))
        }
        peer.add(createGlobalActionComponent(obj, "Invert", { b => !b }))
      }
      Some(item)
    }
  }
}