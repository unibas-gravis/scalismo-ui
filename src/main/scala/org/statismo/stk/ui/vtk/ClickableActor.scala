package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.Point3D

trait ClickableActor extends SingleRenderableActor {
  def clicked(point: Point3D)
}