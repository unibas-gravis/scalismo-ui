package scalismo.ui.vtk

import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.{ ColorProperty, OpacityProperty }

trait ColorableActor extends SingleRenderableActor {
  def color: ColorProperty

  def opacity: OpacityProperty

  listenTo(color, opacity)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s.eq(color) || s.eq(opacity)) setAppearance()
  }

  setAppearance()

  private def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetOpacity(opacity.value)
    val c = color.value.getColorComponents(null)
    vtkActor.GetProperty().SetColor(c(0), c(1), c(2))
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(color, opacity)
    super.onDestroy()
  }
}