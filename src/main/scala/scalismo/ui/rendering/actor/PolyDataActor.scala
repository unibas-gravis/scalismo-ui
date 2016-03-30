package scalismo.ui.rendering.actor

import vtk.{ vtkActor, vtkPolyDataMapper }

class PolyDataActor extends vtkActor {
  val mapper: vtkPolyDataMapper = new vtkPolyDataMapper

  SetMapper(mapper)
  GetProperty().SetInterpolationToGouraud()

}
