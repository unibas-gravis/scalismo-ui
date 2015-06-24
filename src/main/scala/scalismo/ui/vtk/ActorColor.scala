package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.ColorProperty

trait ActorColor extends SingleRenderableActor {
  def color: ColorProperty

  listenTo(color)

  reactions += {
    case VisualizationProperty.ValueChanged(s) if s eq color => setAppearance()
  }

  setAppearance()

  private def setAppearance() = this.synchronized {
    val c = color.value.getColorComponents(null)
    vtkActor.GetProperty().SetColor(c(0), c(1), c(2))
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(color)
    super.onDestroy()
  }
}
