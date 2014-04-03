package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.visualization.props.LineThicknessProperty
import org.statismo.stk.ui.visualization.VisualizationProperty

trait LineActor extends ColorableActor {
  def lineThickness: LineThicknessProperty


  listenTo(lineThickness)

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s.eq(lineThickness)) setAppearance()
  }

  setAppearance()

  private def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetLineWidth(lineThickness.value.toFloat)
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(color, opacity)
    super.onDestroy()
  }
}