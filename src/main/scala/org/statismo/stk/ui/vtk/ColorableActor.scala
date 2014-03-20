package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.visualization.props.ColorProperty
import org.statismo.stk.ui.visualization.VisualizationProperty

trait ColorableActor extends SingleRenderableActor {
  def colorprop: ColorProperty


  listenTo(colorprop)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s eq colorprop) setAppearance()
  }

  setAppearance()

  def setAppearance() = this.synchronized {
    //vtkActor.GetProperty().SetOpacity(colorprop.opacity)
    val color = colorprop.value.getColorComponents(null)
    vtkActor.GetProperty().SetColor(color(0), color(1), color(2))
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(colorprop)
    super.onDestroy()
  }
}