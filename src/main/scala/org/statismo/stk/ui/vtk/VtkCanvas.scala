package org.statismo.stk.ui.vtk

import vtk.vtkCanvas
import vtk.vtkGenericRenderWindowInteractor
import vtk.vtkInteractorStyleTrackballCamera
import org.statismo.stk.ui.Workspace
import org.statismo.stk.ui.Viewport
import java.awt.Graphics
import java.awt.Color

class VtkCanvas(viewport: Viewport) extends vtkCanvas {
  lazy val interactor = new VtkRenderWindowInteractor(viewport)
  iren = interactor

  iren.SetRenderWindow(rw);
  iren.SetSize(this.getSize().width, this.getSize().height);
  iren.ConfigureEvent();
  iren.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)

  private var isEmpty = true

  override def Render() {
    isEmpty = false
    super.Render()
  }

  def setAsEmpty() {
    isEmpty = true
    repaint
  }

  override def paint(g: Graphics) {
    if (isEmpty) {
      g.setColor(Color.BLACK)
      g.fillRect(0, 0, getWidth(), getHeight())
    } else {
      super.paint(g)
    }
  }
}