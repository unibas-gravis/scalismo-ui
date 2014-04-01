package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.visualization.Renderable

trait VtkRenderable extends Renderable {
  def getVtkActor()(implicit vtkViewport: VtkViewport): RenderableActor
}
