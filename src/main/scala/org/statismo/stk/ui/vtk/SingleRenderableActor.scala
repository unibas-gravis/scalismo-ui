package org.statismo.stk.ui.vtk

import vtk.vtkActor

trait SingleRenderableActor extends RenderableActor {
  lazy val vtkActor: vtkActor = ???
  // this MUST be overridden. You'll get an exception if you don't.
  override lazy val vtkActors = Seq(vtkActor)

  // why on earth is this needed? (if we don't define it, we get an AbstractMethodError e.g. in ColorableActor :-O)
  override def onDestroy() = super.onDestroy()
}