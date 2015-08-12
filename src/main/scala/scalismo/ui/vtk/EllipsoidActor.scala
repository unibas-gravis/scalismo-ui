package scalismo.ui.vtk

import scalismo.geometry.{ SquareMatrix, _3D }
import scalismo.ui.visualization.props._
import scalismo.ui.visualization.{ EllipsoidLike, VisualizationProperty }
import scalismo.ui.{ BoundingBox, TwoDViewport }
import vtk._

object EllipsoidActor {
  def apply(vtkViewport: VtkViewport, source: EllipsoidLike): RenderableActor = {
    vtkViewport.viewport match {
      case vp2d: TwoDViewport => new EllipsoidActor2D(vp2d, source)
      case _ => new EllipsoidActor3D(source)
    }
  }
}

trait EllipsoidActor extends ActorColor with ActorOpacity with RotatableActor {
  def source: EllipsoidLike

  override lazy val color: ColorProperty = source.color
  override lazy val opacity: OpacityProperty = source.opacity
  override lazy val rotation: RotationProperty = source.rotation

  private val ellipsoid = new vtkParametricEllipsoid
  private val functionSource = new vtkParametricFunctionSource
  protected val transformFilter = new vtkTransformFilter()
  private val transform = new vtkTransform

  // wire everything together
  functionSource.SetParametricFunction(ellipsoid)
  transformFilter.SetInputConnection(functionSource.GetOutputPort())
  transformFilter.SetTransform(transform)

  override def onRotationChanged(rotation: RotationProperty): Unit = rerender(true)

  protected def rerender(geometryChanged: Boolean) = {
    if (geometryChanged) {
      val (xRadius, yRadius, zRadius) = {
        val l = source.radiuses.value
        (l(0), l(1), l(2))
      }

      val rotationMatrix: Option[SquareMatrix[_3D]] = source.rotation.value

      transform.Identity()
      transform.PostMultiply()

      // if a rotation matrix is set, apply it
      rotationMatrix.foreach { m =>
        val matrix = new vtkMatrix4x4
        // vtk uses 4x4 "homogeneous coordinates", so zero out the last row and column...
        matrix.Zero()
        //... , set the very last element to 1....
        matrix.SetElement(3, 3, 1)
        // ... and fill the rest with the actual data.
        for (r <- 0 to 2; c <- 0 to 2) {
          matrix.SetElement(r, c, m(r, c))
        }
        transform.SetMatrix(matrix)
        matrix.Delete()
      }
      transform.Translate(source.center(0), source.center(1), source.center(2))

      ellipsoid.SetXRadius(xRadius)
      ellipsoid.SetYRadius(yRadius)
      ellipsoid.SetZRadius(zRadius)
    }

    publishEdt(VtkContext.RenderRequest(this))
  }

  listenTo(source, source.radiuses)

  override def onDestroy() = this.synchronized {
    deafTo(source, source.radiuses)
    super.onDestroy()
  }

  reactions += {
    case EllipsoidLike.CenterChanged(s) => rerender(true)
    case VisualizationProperty.ValueChanged(s) => if (s eq source.radiuses) rerender(true)
  }

  protected def onInstantiated(): Unit

  onInstantiated()

  rerender(true)
}

class EllipsoidActor2D(viewport: TwoDViewport, override val source: EllipsoidLike) extends TwoDSlicingActor(viewport) with EllipsoidActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = source.lineWidth

  override protected def onSlicePositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(transformFilter.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = {
    transformFilter.Update()
    VtkUtils.bounds2BoundingBox(transformFilter.GetOutput().GetBounds())
  }

}

class EllipsoidActor3D(override val source: EllipsoidLike) extends SinglePolyDataActor with EllipsoidActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(transformFilter.GetOutputPort())
  }
}
