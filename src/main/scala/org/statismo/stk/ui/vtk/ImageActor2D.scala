package org.statismo.stk.ui.vtk

import _root_.vtk._
import org.statismo.stk.core.geometry.{_3D, Point}
import org.statismo.stk.core.utils.ImageConversion
import org.statismo.stk.ui._
import org.statismo.stk.ui.vtk.ImageActor2D.InstanceData
import org.statismo.stk.ui.vtk.VtkContext.{RenderRequest, ResetCameraRequest}

object ImageActor2D {
  def apply(source: Image3D[_])(implicit vtkViewport: VtkViewport): ImageActor2D = {
    val axis = vtkViewport.viewport.asInstanceOf[TwoDViewport].axis
    new ImageActor2D(source, axis, true)
  }

  def apply(source: Image3D[_], axis: Axis.Value) = new ImageActor2D(source, axis, false)

  final val OutOfBounds: Int = -1

  class InstanceData(source: Image3D[_], axis: Axis.Value) {
    val points: vtkStructuredPoints = Caches.ImageCache.getOrCreate(source.peer, ImageConversion.imageTovtkStructuredPoints(source.asFloatImage))
    lazy val (min, max, exmax, eymax, ezmax) = {
      val b = points.GetBounds()
      val t = points.GetExtent()
      axis match {
        case Axis.X => (b(0), b(1), t(1), t(3), t(5))
        case Axis.Y => (b(2), b(3), t(1), t(3), t(5))
        case Axis.Z => (b(4), b(5), t(1), t(3), t(5))
      }
    }

    val slice = new vtkImageDataGeometryFilter
    slice.SetInputData(points)
    slice.ThresholdValueOff()
    slice.ThresholdCellsOff()

    val intensityRange = Caches.ImageIntensityRangeCache.getOrCreate(points, {
      // this required to correctly show reasonable grayscale values, but is expensive -- which is why it's cached.
      slice.SetExtent(0, exmax, 0, eymax, 0, ezmax)
      slice.Update()
      val r = slice.GetOutput().GetScalarRange()
      (r(0), r(1))
    })

    slice.SetExtent(0, 0, 0, 0, 0, 0)
    slice.Update()

    val grayscale = new vtkLookupTable
    grayscale.SetTableRange(intensityRange._1, intensityRange._2)
    grayscale.SetSaturationRange(0, 0)
    grayscale.SetHueRange(0, 0)
    grayscale.SetValueRange(0, 1)
    grayscale.Build()
  }
}

class ImageActor2D private[ImageActor2D](source: Image3D[_], axis: Axis.Value, isStandalone: Boolean) extends PolyDataActor with ClickableActor {

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
    mapper.SetScalarRange(data.grayscale.GetTableRange())
    mapper.SetLookupTable(data.grayscale)
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
    publishEdt(if (isStandalone) new ResetCameraRequest(this) else new RenderRequest(this))
  }

  listenTo(source.scene, source)
  reload()

  reactions += {
    case Scene.SlicingPosition.PointChanged(sp) =>
      update(sp.point)
    case Image3D.Reloaded(img) =>
      data = new InstanceData(img, axis)
      reload()
  }

  override def onDestroy() {
    deafTo(source.scene, source)
    super.onDestroy()
  }

  override def clicked(point: Point[_3D]) = source.addLandmarkAt(point)
}