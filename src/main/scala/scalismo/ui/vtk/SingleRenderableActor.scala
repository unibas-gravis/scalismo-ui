package scalismo.ui.vtk

import vtk.vtkActor

trait SingleRenderableActor extends RenderableActor {
  def vtkActor: vtkActor

  final override lazy val vtkActors = Seq(vtkActor)

}
