package scalismo.ui.vtk

import java.awt.Color
import java.util.{ Timer, TimerTask }

import scalismo.ui.Scene
import scalismo.ui.util.EdtUtil

import scala.swing.Reactor
import scala.util.Try

class VtkCanvas(parent: VtkPanel) extends VtkJoglPanelComponent(parent) {

  private var deferredRenderingAllowed = false
  private val deferredRenderer = new DeferredRenderer

  private class ColorUpdater(scene: Scene) extends Reactor {
    listenTo(scene)
    reactions += {
      case Scene.DisplayOptions.BackgroundColorChanged(s, nc) if s eq scene =>
        updateBackground(nc)
        RenderReal()
    }

    def updateBackground(color: Color): Unit = {
      val c = color.getColorComponents(null)
      if (renderer != null) renderer.SetBackground(c(0), c(1), c(2))
    }
  }

  // unfortunately, we can't initialize this thing directly, but have to do it lazily -- we use the RenderReal method for that.
  private var colorUpdater: Option[ColorUpdater] = None

  def disableDeferredRendering() = {
    deferredRenderingAllowed = false
  }

  override def Render() = {
    render(immediately = true)
  }

  def render(immediately: Boolean = false) = {
    if (immediately || !deferredRenderingAllowed) {
      RenderReal()
      if (immediately) {
        deferredRenderingAllowed = true
      }
    } else {
      deferredRenderer.request()
    }
  }

  private def RenderReal() = {
    EdtUtil.onEdt {
      // handle color updater
      if (colorUpdater.isEmpty) Try {
        parent.viewportOption.foreach { vp =>
          val cu = new ColorUpdater(vp.scene)
          cu.updateBackground(vp.scene.options.display.backgroundColor)
          colorUpdater = Some(cu)
        }
      }
      super.Render()
    }
  }

  private object DeferredRenderer {
    // this should be almost unnoticeable for humans, but helps to prevent lags
    // which would be caused by large amounts of render requests arriving at the same time.
    val delayMs = 25
  }

  private class DeferredRenderer extends Timer(true) {

    private[DeferredRenderer] class Skipped {
      var count: Int = 0
    }

    val skipped = new Skipped

    private var pending: Option[DeferredRenderTask] = None

    private class DeferredRenderTask extends TimerTask {
      override def run(): Unit = {
        RenderReal()
        skipped.synchronized {
          skipped.count = 0
          pending = None
        }
      }
    }

    def request() = skipped.synchronized {
      if (pending.isEmpty) {
        val task = new DeferredRenderTask
        pending = Some(task)
        super.schedule(task, DeferredRenderer.delayMs)
      } else {
        skipped.count += 1
      }
    }
  }

}
