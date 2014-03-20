package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.visualization.props.{OpacityProperty, ColorProperty}
import org.statismo.stk.ui.visualization.VisualizationProperty

trait ColorableActor extends SingleRenderableActor {
  def color: ColorProperty
  def opacity: OpacityProperty


  listenTo(color, opacity)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s.eq(color) || s.eq(opacity)) setAppearance()
  }

  setAppearance()

  def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetOpacity(opacity.value)
    val c = color.value.getColorComponents(null)
    vtkActor.GetProperty().SetColor(c(0), c(1), c(2))
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(color, opacity)
    super.onDestroy()
  }
}