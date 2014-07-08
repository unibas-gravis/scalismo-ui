package org.statismo.stk.ui.vtk

import java.awt.Color
import java.awt.Graphics


import vtk.vtkCanvas
import vtk.vtkInteractorStyleTrackballCamera
import scala.swing.Swing
import java.util.{TimerTask, Timer}
import javax.swing.SwingUtilities

class VtkCanvas(parent: VtkPanel) extends vtkCanvas {
  lazy val interactor = new VtkRenderWindowInteractor(parent)
  iren = interactor

  iren.SetRenderWindow(rw)
  iren.SetSize(this.getSize.width, this.getSize.height)
  iren.ConfigureEvent()
  iren.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)

  //this.LightFollowCameraOn()

  private def RenderReal() = {
    def doIt() = {
      super.Render()
    }
    if (SwingUtilities.isEventDispatchThread) {
      doIt()
    } else {
      Swing.onEDTWait(doIt())
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
          if (skipped.count > 0) {
            //FIXME
            //println(s"DEBUG: Skipped ${skipped.count} render requests")
          }
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

  private val deferredRenderer = new DeferredRenderer

  override def Render() = {
    render(immediately = true)
  }

  /* This is a somewhat awkward combination, but it seems to do the trick. */

  private var _allowDeferredRendering = false

  def disableDeferredRendering() = {
    _allowDeferredRendering = false
  }

  def render(immediately: Boolean = false) = {
    if (immediately || !_allowDeferredRendering) {
      RenderReal()
      if (immediately) {
        _allowDeferredRendering = true
      }
    } else {
      deferredRenderer.request()
    }
  }

  override def paint(g: Graphics) = this.synchronized {
    g.setColor(Color.BLACK)
    g.fillRect(0, 0, getWidth, getHeight)
    super.paint(g)
  }
}