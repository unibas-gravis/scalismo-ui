package org.statismo.stk.ui.vtk

import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.utils.ImageConversion
import org.statismo.stk.ui.ThreeDImageAxis
import org.statismo.stk.ui.ThreeDImagePlane
import vtk.vtkImagePlaneWidget
import org.statismo.stk.ui.SliceViewport

class ImagePlaneActor(peer: ThreeDImagePlane[_])(implicit interactor: VtkRenderWindowInteractor) extends RenderableActor {
  override val vtkActors = Seq()

  val widget = new vtkImagePlaneWidget
  val img: DiscreteScalarImage3D[Double] = peer.parent.peer.map {
    v => peer.parent.scalarValue.toDouble(v)
  }
  val sp = ImageConversion.image3DTovtkStructuredPoints(img)
  widget.SetInputData(sp)
  peer.axis match {
    case ThreeDImageAxis.X =>
      peer.maxPosition = sp.GetDimensions()(0)
      widget.SetPlaneOrientationToXAxes()
    case ThreeDImageAxis.Y =>
      peer.maxPosition = sp.GetDimensions()(1)
      widget.SetPlaneOrientationToYAxes()
    case ThreeDImageAxis.Z =>
      peer.maxPosition = sp.GetDimensions()(2)
      widget.SetPlaneOrientationToZAxes()
  }

  widget.SetSliceIndex(peer.position)
  widget.SetInteractor(interactor)
  widget.On()

  listenTo(peer, interactor)

  reactions += {
    case ThreeDImagePlane.PositionChanged(s) => this.synchronized {
      widget.SetSliceIndex(peer.position)
      if (interactor.viewport.isInstanceOf[SliceViewport]) {
        publish(new VtkContext.ResetCameraRequest(this))
      } else {
        publish(new VtkContext.RenderRequest(this))
      }
    }
    case VtkRenderWindowInteractor.PointClicked(point) => this.synchronized {
      val own = widget.GetSlicePosition().toFloat
      val cmp = peer.axis match {
        case ThreeDImageAxis.X => point.x
        case ThreeDImageAxis.Y => point.y
        case ThreeDImageAxis.Z => point.z
      }
      if (Math.abs(own - cmp) < 0.001) {
        peer.addLandmarkAt(point)
      }
    }
  }

  override def onDestroy() = this.synchronized {
    deafTo(peer, interactor)
    widget.Off()
    widget.Delete()
    super.onDestroy()
  }
}