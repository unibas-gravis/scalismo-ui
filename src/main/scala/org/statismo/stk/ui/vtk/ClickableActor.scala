package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.{_3D, Point}

trait ClickableActor extends SingleRenderableActor {
  def clicked(point: Point[_3D])
}