package org.statismo.stk.ui.vtk

import vtk.{vtkParametricFunctionSource, vtkParametricEllipsoid, vtkSphereSource}
import org.statismo.stk.ui.visualization.{VisualizationProperty, SphereLike}

class SphereActor(source: SphereLike) extends PolyDataActor with ColorableActor {
//  private lazy val sphere = new vtkSphereSource
  private lazy val sphere = new vtkParametricEllipsoid
  private lazy val sphereSource = new vtkParametricFunctionSource

  sphereSource.SetParametricFunction(sphere)
  sphereSource.Update()

  override lazy val color = source.color
  override lazy val opacity = source.opacity
  lazy val radius = source.radius
  listenTo(source, radius)

//  mapper.SetInputConnection(sphere.GetOutputPort())
  mapper.SetInputConnection(sphereSource.GetOutputPort())
  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  reactions += {
    case SphereLike.CenterChanged(s) => setGeometry()
    case VisualizationProperty.ValueChanged(s) => if (s eq radius) setGeometry()
  }

  def setGeometry() = this.synchronized {
//    sphere.S
//    sphere.SetCenter(source.center(0), source.center(1), source.center(2))

    this.SetPosition(source.center(0), source.center(1), source.center(2))
    sphere.SetXRadius(source.radius.value)
    sphere.SetYRadius(source.radius.value)
    sphere.SetZRadius(source.radius.value)
    sphere.Modified()
    sphereSource.Modified()
    mapper.Modified()
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(source, radius)
    super.onDestroy()
    sphere.Delete()
    sphereSource.Delete()
  }
}