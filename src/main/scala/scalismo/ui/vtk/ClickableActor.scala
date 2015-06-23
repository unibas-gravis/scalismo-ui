package scalismo.ui.vtk

import scalismo.geometry.{ Point, _3D }

trait ClickableActor extends SingleRenderableActor {
  def clicked(point: Point[_3D]): Unit
}