package scalismo.ui.vtk

import vtk.{ vtkActor, vtkPolyDataMapper }

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

  override def onDestroy() = {
    super.onDestroy()
    mapper.Delete()
  }
}