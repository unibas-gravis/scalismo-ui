package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.OpacityProperty

trait ActorOpacity extends SingleRenderableActor {
  def opacity: OpacityProperty

  listenTo(opacity)

  reactions += {
    case VisualizationProperty.ValueChanged(s) if s eq opacity => setAppearance()
  }

  setAppearance()

  private def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetOpacity(opacity.value)
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(opacity)
    super.onDestroy()
  }
}