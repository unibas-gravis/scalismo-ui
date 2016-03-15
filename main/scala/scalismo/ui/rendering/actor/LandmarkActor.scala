package scalismo.ui.rendering.actor

import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties.{ ColorProperty, LineWidthProperty, OpacityProperty }
import scalismo.ui.model.{ BoundingBox, LandmarkNode }
import scalismo.ui.rendering.actor.mixin.{ ActorColor, ActorLineWidth, ActorOpacity }
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import vtk._

object LandmarkActor extends SimpleActorsFactory[LandmarkNode] {
  override def actorsFor(renderable: LandmarkNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new LandmarkActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new LandmarkActor2D(renderable, _2d))
    }
  }
}

trait LandmarkActor extends ActorColor with ActorOpacity {
  //with RotatableActor {
  def node: LandmarkNode

  override lazy val color: ColorProperty = node.color
  override lazy val opacity: OpacityProperty = node.opacity
  //override lazy val rotation: RotationProperty = node.rotation

  private val ellipsoid = new vtkParametricEllipsoid
  private val functionSource = new vtkParametricFunctionSource
  protected val transformFilter = new vtkTransformFilter()
  private val transform = new vtkTransform

  // wire everything together
  functionSource.SetParametricFunction(ellipsoid)
  transformFilter.SetInputConnection(functionSource.GetOutputPort())
  transformFilter.SetTransform(transform)

  //override def onRotationChanged(rotation: RotationProperty): Unit = rerender(true)

  protected def rerender(geometryChanged: Boolean) = {
    if (geometryChanged) {
      val (xRadius, yRadius, zRadius) = {
        // FIXME: radiuses
        //val l = node.radiuses.value
        val l = Array.fill(3)(3.0f)
        (l(0), l(1), l(2))
      }

      transform.Identity()
      transform.PostMultiply()

      //val rotationMatrix: Option[SquareMatrix[_3D]] = node.rotation.value
      // if a rotation matrix is set, apply it
      //      rotationMatrix.foreach { m =>
      //        val matrix = new vtkMatrix4x4
      //        // vtk uses 4x4 "homogeneous coordinates", so zero out the last row and column...
      //        matrix.Zero()
      //        //... , set the very last element to 1....
      //        matrix.SetElement(3, 3, 1)
      //        // ... and fill the rest with the actual data.
      //        for (r <- 0 to 2; c <- 0 to 2) {
      //          matrix.SetElement(r, c, m(r, c))
      //        }
      //        transform.SetMatrix(matrix)
      //      }

      val center = node.transformedSource.point
      transform.Translate(center(0), center(1), center(2))

      ellipsoid.SetXRadius(xRadius)
      ellipsoid.SetYRadius(yRadius)
      ellipsoid.SetZRadius(zRadius)
    }

    actorChanged(geometryChanged)
  }

  listenTo(node)

  //, node.radiuses)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(true)
    //case VisualizationProperty.ValueChanged(s) => if (s eq node.radiuses) rerender(true)
  }

  protected def onInstantiated(): Unit

  onInstantiated()

  rerender(true)
}

class LandmarkActor2D(override val node: LandmarkNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with LandmarkActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = node.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(transformFilter.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = {
    transformFilter.Update()
    VtkUtil.bounds2BoundingBox(transformFilter.GetOutput().GetBounds())
  }

}

class LandmarkActor3D(override val node: LandmarkNode) extends SinglePolyDataActor with LandmarkActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(transformFilter.GetOutputPort())
  }
}

