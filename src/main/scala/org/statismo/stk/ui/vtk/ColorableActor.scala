package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.Colorable

trait ColorableActor extends SingleDisplayableActor {
  lazy val colorable: Colorable = null

  listenTo(colorable)

  reactions += {
    case Colorable.ColorChanged(c) => setAppearance()
  }

  setAppearance()

  def setAppearance() = this.synchronized {
    vtkActor.GetProperty().SetOpacity(colorable.opacity)
    val color = colorable.color.getColorComponents(null)
    vtkActor.GetProperty().SetColor(color(0), color(1), color(2))
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(colorable)
    super.onDestroy()
  }
}