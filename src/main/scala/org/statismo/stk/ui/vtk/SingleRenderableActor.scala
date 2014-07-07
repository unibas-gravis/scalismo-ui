package org.statismo.stk.ui.vtk

import vtk.vtkActor
import org.statismo.stk.ui.BoundingBox

trait SingleRenderableActor extends RenderableActor {
  // this MUST be overridden. You'll get an exception if you don't.
  lazy val vtkActor: vtkActor = ???

  override lazy val vtkActors = Seq(vtkActor)

  private var _empty = false

  protected def empty = _empty

  protected def empty_=(nv: Boolean) = {
    _empty = nv
  }

  override def currentBoundingBox = {
    if (empty) BoundingBox.None else VtkUtils.bounds2BoundingBox(vtkActor.GetBounds())
  }

  override def onDestroy() = {
    super.onDestroy()
  }


}