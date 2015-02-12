package scalismo.ui.vtk

import java.util.{ Timer, TimerTask }

import scalismo.ui.util.EdtUtil

class VtkCanvas(parent: VtkPanel) extends VtkJoglPanelComponent(parent) {

  private var deferredRenderingAllowed = false
  private val deferredRenderer = new DeferredRenderer

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
          //          if (skipped.count > 0) {
          //            println(s"DEBUG: Skipped ${skipped.count} render requests")
          //          }
          skipped.count = 0
          pending = None
        }
      }
    }

    def request() = skipped.synchronized {
      if (!pending.isDefined) {
        val task = new DeferredRenderTask
        pending = Some(task)
        super.schedule(task, DeferredRenderer.delayMs)
      } else {
        skipped.count += 1
      }
    }
  }

}