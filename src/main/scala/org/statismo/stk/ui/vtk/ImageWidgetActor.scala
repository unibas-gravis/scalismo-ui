package org.statismo.stk.ui.vtk

import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.utils.ImageConversion
import scala.collection.{mutable, immutable}
import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event

//import org.statismo.stk.ui.ThreeDImageAxis
//import org.statismo.stk.ui.ThreeDImagePlane
import vtk.vtkImagePlaneWidget
import org.statismo.stk.ui.{Scene, Axis, Image3D, TwoDViewport}

class ImageWidgetActor(peer: Image3D.Renderable3D)(implicit interactor: VtkRenderWindowInteractor) extends RenderableActor {
  override val vtkActors = Seq()

  val points = ImageConversion.image3DTovtkStructuredPoints(peer.source.asFloatImage)
  lazy val (xmin , xmax, ymin, ymax, zmin, zmax) = {
    val b = points.GetBounds()
    (b(0),b(1),b(2),b(3),b(4),b(5))
  }

  private [ImageWidgetActor] class Widget(val axis: Axis.Value) extends vtkImagePlaneWidget {
    SetInputData(points)
    axis match {
      case Axis.X => SetPlaneOrientationToXAxes()
      case Axis.Y => SetPlaneOrientationToYAxes()
      case Axis.Z => SetPlaneOrientationToZAxes()
    }
    SetSliceIndex(0)
    SetInteractor(interactor)
    On()

    def contains(point: Point3D) : Boolean = {
      val own = GetSlicePosition().toFloat
      val cmp = axis match {
        case Axis.X => point.x
        case Axis.Y => point.y
        case Axis.Z => point.z
      }
      return Math.abs(own - cmp) < 0.001
    }

    def setSlicePoint(where: Point3D) : Unit = {
      val np: Float = axis match {
        case Axis.X => where.x
        case Axis.Y => where.y
        case Axis.Z => where.z
      }
      SetSlicePosition(np)
      publish (VtkContext.RenderRequest(ImageWidgetActor.this))
    }
  }

  val x = new Widget(Axis.X)
  val y = new Widget(Axis.Y)
  val z = new Widget(Axis.Z)
  val widgets: immutable.Seq[Widget] = immutable.Seq(x,y,z)

  listenTo(interactor, peer.source.scene)

  reactions += {
    case VtkRenderWindowInteractor.PointClicked(point) => this.synchronized {
      def in(v: Double, min: Double, max: Double) = {
        min <= v && v <= max
      }
      if (in(point.x, xmin, xmax) && in(point.y, ymin, ymax) && in (point.z, zmin, zmax)) {
        // point can in principle qualify.
        if (widgets.foldLeft(false){case (b,w) => b || w.contains(point)}) {
          peer.source.addLandmarkAt(point)
        }
      }
    }
    case Scene.SlicingPosition.BoundingBoxChanged(s) => {
      println("FIXME: Handle Bounding Box Changes in ImageWidgetActor")
    }
    case Scene.SlicingPosition.PrecisionChanged(s) => {
      println("FIXME: Handle Precision Changes in ImageWidgetActor")
    }
    case Scene.SlicingPosition.PointChanged(s) => {
      widgets.foreach {_.setSlicePoint(s.point)}
    }
  }
  override def onDestroy() = this.synchronized {
    deafTo(interactor, peer.source.scene)
    widgets.foreach { widget =>
      widget.Off()
      widget.Delete()
    }
    super.onDestroy()
  }

  override def currentBoundingBox = VtkUtils.bounds2BoundingBox(points.GetBounds())
}