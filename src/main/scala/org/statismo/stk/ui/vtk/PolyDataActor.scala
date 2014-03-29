package org.statismo.stk.ui.vtk

import vtk.vtkActor
import vtk.vtkPolyDataMapper
import scala.util.Try

class PolyDataActor extends vtkActor with SingleRenderableActor {
  override lazy val vtkActor = this
  lazy val mapper = new vtkPolyDataMapper
  vtkActor.SetMapper(mapper)

  override def currentBoundingBox = {
    //FIXME: don't know why it sometimes fails
    empty = Try(mapper.GetInput().GetPoints().GetNumberOfPoints() == 0).getOrElse(false)
    super.currentBoundingBox
  }
}