package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.LineWidthProperty

trait ActorLineWidth extends SingleRenderableActor {
  def lineWidth: LineWidthProperty

  listenTo(lineWidth)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s.eq(lineWidth)) setAppearance()
  }

  setAppearance()

  private def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetLineWidth(lineWidth.value.toFloat)
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(lineWidth)
    super.onDestroy()
  }
}