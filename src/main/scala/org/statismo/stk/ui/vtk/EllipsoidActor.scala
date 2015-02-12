package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.{_3D, SquareMatrix}
import org.statismo.stk.ui.visualization.props.{RadiusesProperty, ColorProperty, OpacityProperty, RotationProperty}
import vtk._
import org.statismo.stk.ui.visualization.{VisualizationProperty, EllipsoidLike}

class EllipsoidActor(source: EllipsoidLike) extends PolyDataActor with ColorableActor with RotatableActor {
  private lazy val ellipsoid = new vtkParametricEllipsoid
  private lazy val functionSource = new vtkParametricFunctionSource

  functionSource.SetParametricFunction(ellipsoid)
  functionSource.Update()

  override lazy val color: ColorProperty = source.color
  override lazy val opacity: OpacityProperty = source.opacity
  override lazy val rotation: RotationProperty = source.rotation

  override def onRotationChanged(rotation: RotationProperty): Unit = setGeometry()

  lazy val radiuses: RadiusesProperty[_3D] = source.radiuses
  listenTo(source, radiuses)

  this.SetBackfaceProperty(this.GetProperty())

  mapper.SetInputConnection(functionSource.GetOutputPort())
  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  reactions += {
    case EllipsoidLike.CenterChanged(s) => setGeometry()
    case VisualizationProperty.ValueChanged(s) => if (s eq radiuses) setGeometry()
  }



  def setGeometry() = this.synchronized {
    val (xRadius, yRadius, zRadius) = {
      val l = source.radiuses.value
      (l(0),l(1),l(2))
    }

    val rotationMatrix: Option[SquareMatrix[_3D]] = source.rotation.value

    val transform = new vtkTransform
    transform.PostMultiply()

    // if a rotation matrix is set, apply it
    rotationMatrix.map { m=>
      val matrix = new vtkMatrix4x4
      // vtk uses 4x4 "homogeneous coordinates", so zero out the last row and column...
      matrix.Zero()
      //... , set the very last element to 1....
      matrix.SetElement(3,3,1)
      // ... and fill the rest with the actual data.
      for (r <- 0 to 2; c <- 0 to 2) {
        matrix.SetElement(r,c, m(r,c))
      }
      transform.SetMatrix(matrix)
      matrix.Delete()
    }

    transform.Translate(source.center(0), source.center(1), source.center(2))
    this.SetUserTransform(transform)
    transform.Delete()


    ellipsoid.SetXRadius(xRadius)
    ellipsoid.SetYRadius(yRadius)
    ellipsoid.SetZRadius(zRadius)
    ellipsoid.Modified()
    functionSource.Modified()
    mapper.Modified()

    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(source, radiuses)
    super.onDestroy()
    ellipsoid.Delete()
    functionSource.Delete()
  }
}