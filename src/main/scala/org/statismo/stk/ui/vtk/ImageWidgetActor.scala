package org.statismo.stk.ui.vtk

import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.utils.ImageConversion
import scala.collection.{mutable, immutable}
import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event
import scala.swing.Swing

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

  def in(v: Double, min: Double, max: Double) = {
    min <= v && v <= max
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
      val (np, min, max) = axis match {
        case Axis.X => (where.x, xmin, xmax)
        case Axis.Y => (where.y, ymin, ymax)
        case Axis.Z => (where.z, zmin, zmax)
      }
      if (in(np, min, max)) {
        On()
        SetSlicePosition(np)
      } else {
        Off()
      }
      publish (VtkContext.RenderRequest(ImageWidgetActor.this))
    }

    private var on = false
    override def On() = this.synchronized{
      if (!on) {
        on = true
        super.On()
      }
    }
    override def Off() = this.synchronized{
//      if (on) {
//        on = false
//        super.Off()
//      }
    }
  }

  val x = new Widget(Axis.X)
  val y = new Widget(Axis.Y)
  val z = new Widget(Axis.Z)
  val widgets: immutable.Seq[Widget] = immutable.Seq(x,y,z)

  listenTo(interactor, peer.source.scene)

  reactions += {
    case VtkRenderWindowInteractor.PointClicked(point) => this.synchronized {
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
    case Scene.SlicingPosition.PointChanged(s) => Swing.onEDT{
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