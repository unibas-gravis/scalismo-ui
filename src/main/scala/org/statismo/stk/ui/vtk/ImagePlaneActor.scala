package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.ThreeDImagePlane
import vtk.vtkImagePlaneWidget
import org.statismo.stk.ui.ThreeDImageAxis
import org.statismo.stk.core.utils.ImageConversion
import vtk.vtkOpenGLActor
import org.statismo.stk.ui.Removeable
import org.statismo.stk.core.common.ScalarValue
import scala.reflect.ClassTag
import reflect.runtime.universe.{ TypeTag, typeOf }
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.image.DiscreteScalarImage3D

class ImagePlaneActor(peer: ThreeDImagePlane[_])(implicit interactor: VtkRenderWindowInteractor) extends DisplayableActor {
  override val vtkActors = Seq()
		  
  val widget = new vtkImagePlaneWidget
  val img : DiscreteScalarImage3D[Double] = peer.parent.peer.map(v => peer.parent.scalarValue.toDouble(v))
  val sp = ImageConversion.image3DTovtkStructuredPoints(img)
  widget.SetInputData(sp)
  peer.axis match {
    case ThreeDImageAxis.X => {
      peer.maxPosition = sp.GetDimensions()(0)
      widget.SetPlaneOrientationToXAxes()
    }
    case ThreeDImageAxis.Y => {
      peer.maxPosition = sp.GetDimensions()(1)
      widget.SetPlaneOrientationToYAxes()
    }
    case ThreeDImageAxis.Z => {
      peer.maxPosition = sp.GetDimensions()(2)
      widget.SetPlaneOrientationToZAxes()
    }
  }

  widget.SetSliceIndex(peer.position)
  widget.SetInteractor(interactor)
  widget.On()

  listenTo(peer, interactor)

  reactions += {
    case ThreeDImagePlane.PositionChanged(s) => {
      widget.SetSliceIndex(peer.position)
      publish(new VtkContext.RenderRequest(this))
    }
    case VtkRenderWindowInteractor.PointClicked(point) => {
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

  override def onDestroy() = {
    deafTo(peer, interactor)
    widget.Off()
    widget.Delete()
    super.onDestroy()

  }
}