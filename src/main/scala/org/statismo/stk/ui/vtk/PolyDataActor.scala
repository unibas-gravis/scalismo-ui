package org.statismo.stk.ui.vtk

import vtk.vtkActor
import vtk.vtkPolyDataMapper

class PolyDataActor extends vtkActor with SingleDisplayableActor {
  override lazy val vtkActor = this
  val mapper = new vtkPolyDataMapper
  vtkActor.SetMapper(mapper)
}