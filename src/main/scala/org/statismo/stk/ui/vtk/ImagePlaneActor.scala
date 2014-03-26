package org.statismo.stk.ui.vtk

import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.utils.ImageConversion
import scala.collection.{mutable, immutable}
import org.statismo.stk.core.geometry.Point3D

//import org.statismo.stk.ui.ThreeDImageAxis
//import org.statismo.stk.ui.ThreeDImagePlane
import vtk.vtkImagePlaneWidget
import org.statismo.stk.ui.{Axis, Image3D, TwoDViewport}

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
  }

  val widgets: immutable.Seq[Widget] = {
    val buf = new mutable.ArrayBuffer[Widget]
    if (true) buf += new Widget(Axis.X)
    if (true) buf += new Widget(Axis.Y)
    if (true) buf += new Widget(Axis.Z)
    buf.to[immutable.Seq]
  }

  listenTo(interactor)

  reactions += {
//    case ThreeDImagePlane.PositionChanged(s) => this.synchronized {
//      widget.SetSliceIndex(peer.position)
//      if (interactor.viewport.isInstanceOf[TwoDViewport]) {
//        publish(new VtkContext.ResetCameraRequest(this))
//      } else {
//        publish(new VtkContext.RenderRequest(this))
//      }
//    }
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
  }
  override def onDestroy() = this.synchronized {
    deafTo(interactor)
    widgets.foreach { widget =>
      widget.Off()
      widget.Delete()
    }
    super.onDestroy()
  }
}