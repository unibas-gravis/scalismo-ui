package org.statismo.stk.ui.vtk

import javax.media.opengl.{Threading, GLAutoDrawable, GLContext, GLEventListener}

import org.statismo.stk.ui.util.EdtUtil
import vtk.rendering.jogl.vtkJoglCanvasComponent
import vtk.{vtkInteractorStyleTrackballCamera, vtkObjectBase}

class VtkCanvas2(parent: VtkPanel) extends vtkJoglCanvasComponent {
  def disableDeferredRendering() = {}

  def render(b: Boolean): Unit = {}

  lazy val interactor = {
    val i = new VtkRenderWindowInteractor(parent)
    EdtUtil.onEdt({
      // remove previous interactor
      //windowInteractor.RemoveAllObservers() // this crashes ??!!
      // and replace it with our implementation
      windowInteractor = i
      i.SetRenderWindow(this.renderWindow)
      i.TimerEventResetsTimerOff()
      i.SetSize(200, 200)
      i.ConfigureEvent()
      // Update style
      i.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)
      i.AddObserver("CreateTimerEvent", this.eventForwarder, "StartTimer")
      i.AddObserver("DestroyTimerEvent", this.eventForwarder, "DestroyTimer")
    }
    , wait = true)
    i
  }
}
