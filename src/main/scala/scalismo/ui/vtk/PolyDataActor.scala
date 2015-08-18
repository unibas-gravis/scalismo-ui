package scalismo.ui.vtk

import vtk.{ vtkActor, vtkPolyDataMapper }

abstract class PolyDataActor extends vtkActor with RenderableActor {
  lazy val mapper = new vtkPolyDataMapper
  SetMapper(mapper)

  this.GetProperty().SetInterpolationToGouraud()

}

class SinglePolyDataActor extends PolyDataActor with SingleRenderableActor {
  override lazy val vtkActor = this
}
