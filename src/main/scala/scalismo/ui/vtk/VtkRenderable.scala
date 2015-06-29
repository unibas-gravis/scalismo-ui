package scalismo.ui.vtk

import scalismo.ui.visualization.Renderable

trait VtkRenderable extends Renderable {
  def getVtkActor()(implicit vtkViewport: VtkViewport): RenderableActor
}
