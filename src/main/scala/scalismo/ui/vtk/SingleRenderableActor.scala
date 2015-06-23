package scalismo.ui.vtk

import scalismo.ui.BoundingBox
import vtk.vtkActor

trait SingleRenderableActor extends RenderableActor {
  // this MUST be overridden. You'll get an exception if you don't.
  // (but it also must be a lazy val, not a def, so I had to provide some initial value)
  lazy val vtkActor: vtkActor = throw new NotImplementedError

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