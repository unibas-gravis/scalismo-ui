package org.statismo.stk.ui.vtk

import vtk.vtkCanvas
import vtk.vtkGenericRenderWindowInteractor
import vtk.vtkInteractorStyleTrackballCamera
import org.statismo.stk.ui.Workspace
import org.statismo.stk.ui.Viewport

class VtkCanvas(viewport: Viewport) extends vtkCanvas {
  lazy val interactor = new VtkRenderWindowInteractor(viewport)
  iren = interactor

  iren.SetRenderWindow(rw);
  //        iren.TimerEventResetsTimerOff();
  //        iren.AddObserver("CreateTimerEvent", this, "StartTimer");
  //        iren.AddObserver("DestroyTimerEvent", this, "DestroyTimer");
  iren.SetSize(this.getSize().width, this.getSize().height);
  iren.ConfigureEvent();
  iren.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)
}