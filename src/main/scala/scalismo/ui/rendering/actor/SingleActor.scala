package scalismo.ui.rendering.actor

import vtk.vtkActor

trait SingleActor extends vtkActor with Actors {
  final override val vtkActors: List[vtkActor] = List(this)
}

