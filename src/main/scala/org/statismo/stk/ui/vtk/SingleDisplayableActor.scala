package org.statismo.stk.ui.vtk

import vtk.vtkActor

trait SingleDisplayableActor extends DisplayableActor {
  lazy val vtkActor: vtkActor = null; // this MUST be overridden.
  override lazy val vtkActors = Seq(vtkActor)
}