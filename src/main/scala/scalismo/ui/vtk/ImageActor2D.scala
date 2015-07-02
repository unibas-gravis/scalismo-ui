package scalismo.ui.vtk

import _root_.vtk._
import scalismo.geometry.{ Point, _3D }
import scalismo.ui._
import scalismo.ui.vtk.ImageActor2D.InstanceData
import scalismo.ui.vtk.VtkContext.RenderRequest
import scalismo.utils.ImageConversion

object ImageActor2D {
  def apply(source: Image3DView[_])(implicit vtkViewport: VtkViewport): ImageActor2D = {
    val axis = vtkViewport.viewport.asInstanceOf[TwoDViewport].axis
    new ImageActor2D(source, axis, true)
  }

  def apply(source: Image3DView[_], axis: Axis.Value) = new ImageActor2D(source, axis, false)

  final val OutOfBounds: Int = -1

  class InstanceData(source: Image3DView[_], axis: Axis.Value) {
    val points: vtkStructuredPoints = Caches.ImageCache.getOrCreate(source.source, ImageConversion.imageToVtkStructuredPoints(source.asFloatImage))
    lazy val (min, max, exmax, eymax, ezmax) = {
      val b = points.GetBounds()
      val t = points.GetExtent()
      axis match {
        case Axis.X => (b(0), b(1), t(1), t(3), t(5))
        case Axis.Y => (b(2), b(3), t(1), t(3), t(5))
        case Axis.Z => (b(4), b(5), t(1), t(3), t(5))
      }
    }

    // most of the windowLevel logic is stolen from Slicer :-)
    // https://github.com/Slicer/Slicer/blob/121d28f3d03c418e13826a83df1ea1ffc586f0b7/Libs/MRML/DisplayableManager/vtkSliceViewInteractorStyle.cxx#L355-L370
    val windowLevel = new vtkImageMapToWindowLevelColors()
    windowLevel.SetInputData(points)
    windowLevel.SetWindow(source.scene.imageWindowLevel.window)
    windowLevel.SetLevel(source.scene.imageWindowLevel.level)
    windowLevel.Update()
    windowLevel.SetOutputFormatToLuminance()

    val slice = new vtkImageDataGeometryFilter
    slice.SetInputConnection(windowLevel.GetOutputPort())
    slice.ThresholdValueOff()
    slice.ThresholdCellsOff()
    slice.SetExtent(0, 0, 0, 0, 0, 0)
    slice.Update()

  }

}

class ImageActor2D private[ImageActor2D] (source: Image3DView[_], axis: Axis.Value, isStandalone: Boolean) extends SinglePolyDataActor with ClickableActor {

  var data = new InstanceData(source, axis)

  override def currentBoundingBox = VtkUtils.bounds2BoundingBox(data.points.GetBounds())

  def point3DToExtent(p: Point[_3D], axis: Axis.Value) = {
    val (fmin, fmax, fval, tmax) = axis match {
      case Axis.X => (data.min, data.max, p(0), data.exmax)
      case Axis.Y => (data.min, data.max, p(1), data.eymax)
      case Axis.Z => (data.min, data.max, p(2), data.ezmax)
    }
    val idx = if (fval < fmin || fval > fmax) ImageActor2D.OutOfBounds
    else {
      val (nval, nmax) = (fval - fmin, fmax - fmin)
      Math.floor(nval * tmax / nmax).toInt
    }
    idx
  }

  def reload() = {
    mapper.RemoveAllInputs()
    mapper.SetInputData(data.slice.GetOutput())
    update(source.scene.slicingPosition.point)
  }

  def update(point: Point[_3D]) = {
    val i = point3DToExtent(point, axis)
    if (i == ImageActor2D.OutOfBounds) {
      SetVisibility(0)
    } else {
      SetVisibility(1)
      axis match {
        case Axis.X => data.slice.SetExtent(i, i, 0, data.eymax, 0, data.ezmax)
        case Axis.Y => data.slice.SetExtent(0, data.exmax, i, i, 0, data.ezmax)
        case Axis.Z => data.slice.SetExtent(0, data.exmax, 0, data.eymax, i, i)
      }
      data.slice.Update()
      mapper.Update()
    }
    publishEdt(new RenderRequest(this))
  }

  listenTo(source.scene, source)
  reload()

  reactions += {
    case Scene.SlicingPosition.PointChanged(sp, _, _) =>
      update(sp.point)
    case Image3DView.Reloaded(img) =>
      data = new InstanceData(img, axis)
      reload()
    case Scene.ImageWindowLevel.ImageWindowLevelChanged(_, window, level) =>
      if (data.windowLevel.GetWindow() != window || data.windowLevel.GetLevel() != level) {
        data.windowLevel.SetWindow(window)
        data.windowLevel.SetLevel(level)
        data.windowLevel.Update()
        data.slice.Update()
        mapper.Update()
        publishEdt(new RenderRequest(this))
      }
  }

  override def onDestroy(): Unit = {
    deafTo(source.scene, source)
    super.onDestroy()
  }

  override def clicked(point: Point[_3D]) = source.addLandmarkAt(point)
}
