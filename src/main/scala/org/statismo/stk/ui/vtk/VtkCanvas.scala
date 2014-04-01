package org.statismo.stk.ui.vtk

import java.awt.Color
import java.awt.Graphics

import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.Workspace

import vtk.vtkCanvas
import vtk.vtkInteractorStyleTrackballCamera
import scala.swing.Swing
import java.util.{TimerTask, Timer}

class VtkCanvas(workspace: Workspace, viewport: Viewport) extends vtkCanvas {
  lazy val interactor = new VtkRenderWindowInteractor(workspace, viewport)
  iren = interactor

  iren.SetRenderWindow(rw)
  iren.SetSize(this.getSize.width, this.getSize.height)
  iren.ConfigureEvent()
  iren.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)

  private var _empty = true

  def empty = _empty
  def empty_=(nv: Boolean) = this.synchronized {
    if (empty != nv) {
      _empty = nv
      Render()
    }
  }

  private def RenderReal() = Swing.onEDTWait {
    if (empty) {
      invalidate()
      repaint()
    } else {
      super.Render()
    }
  }

  private object DeferredRenderer {
    // this should be almost unnoticeable for humans, but helps to prevent lags
    // which would be caused by large amounts of render requests arriving at the same time.
    val delayMs = 25
  }

  private class DeferredRenderer extends Timer(true) {

    private [DeferredRenderer] class Skipped {
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

  override def Render() = deferredRenderer.request()

  override def paint(g: Graphics) = this.synchronized {
    if (empty) {
      g.setColor(Color.BLACK)
      g.fillRect(0, 0, getWidth, getHeight)
    } else {
      super.paint(g)
    }
  }
}