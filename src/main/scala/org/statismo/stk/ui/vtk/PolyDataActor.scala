package org.statismo.stk.ui.vtk

import vtk.vtkPolyDataMapper
import vtk.vtkActor

class PolyDataActor extends vtkActor with SingleDisplayableActor {
	override lazy val vtkActor = this
    val mapper = new vtkPolyDataMapper
    vtkActor.SetMapper(mapper)
}