package org.statismo.stk.ui.vtk

import vtk.vtkSphereSource
import org.statismo.stk.ui.visualization.{VisualizationProperty, SphereLike}

class SphereActor(source: SphereLike) extends PolyDataActor with ColorableActor {
  private lazy val sphere = new vtkSphereSource
  override lazy val color = source.color
  override lazy val opacity = source.opacity
  lazy val radius = source.radius
  listenTo(source, radius)

  mapper.SetInputConnection(sphere.GetOutputPort())
  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  reactions += {
    case SphereLike.CenterChanged(s) => setGeometry()
    case VisualizationProperty.ValueChanged(s) => if (s eq radius) setGeometry()
  }

  def setGeometry() = this.synchronized {
    sphere.SetCenter(source.center.x, source.center.y, source.center.z)
    sphere.SetRadius(source.radius.value)
    sphere.Modified()
    mapper.Modified()
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(source, radius)
    super.onDestroy()
  }
}