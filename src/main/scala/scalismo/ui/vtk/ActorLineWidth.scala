package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.LineThicknessProperty

trait ActorLineWidth extends SingleRenderableActor {
  def lineThickness: LineThicknessProperty

  listenTo(lineThickness)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s.eq(lineThickness)) setAppearance()
  }

  setAppearance()

  private def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetLineWidth(lineThickness.value.toFloat)
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(lineThickness)
    super.onDestroy()
  }
}