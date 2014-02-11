package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.Colorable

trait ColorableActor extends SingleDisplayableActor {
  lazy val colorable: Colorable = null

  listenTo(colorable)

  reactions += {
    case Colorable.AppearanceChanged(c) => setAppearance
  }

  setAppearance

  def setAppearance() {
    vtkActor.GetProperty().SetOpacity(colorable.opacity)
    val color = colorable.color.getColorComponents(null)
    vtkActor.GetProperty().SetColor(color(0), color(1), color(2))
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() {
    deafTo(colorable)
    super.onDestroy()
  }
}