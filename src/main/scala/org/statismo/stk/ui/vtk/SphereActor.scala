package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.Radius
import org.statismo.stk.ui.SphereLike

import vtk.vtkSphereSource

class SphereActor(source: SphereLike) extends PolyDataActor with ColorableActor {
  private lazy val sphere = new vtkSphereSource
  override lazy val colorable = source
  listenTo(source)

  mapper.SetInputConnection(sphere.GetOutputPort())
  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  reactions += {
    case SphereLike.CenterChanged(s) => setGeometry()
    case Radius.RadiusChanged(r) => setGeometry()
  }

  def setGeometry() = this.synchronized {
    sphere.SetCenter(source.center.x, source.center.y, source.center.z)
    sphere.SetRadius(source.radius)
    sphere.Modified()
    mapper.Modified()
    publish(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(source)
    super.onDestroy()
  }
}