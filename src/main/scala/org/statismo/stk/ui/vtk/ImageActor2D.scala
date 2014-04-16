package org.statismo.stk.ui.vtk

import org.statismo.stk.core.utils.ImageConversion
import org.statismo.stk.ui._

import _root_.vtk._
import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.ui.vtk.VtkContext.{ResetCameraRequest, RenderRequest}

object ImageActor2D {
  def apply(source: Image3D[_])(implicit vtkViewport: VtkViewport) : ImageActor2D = {
    val points = ImageConversion.image3DTovtkStructuredPoints(source.asFloatImage)
    val axis = vtkViewport.viewport.asInstanceOf[TwoDViewport].axis
    new ImageActor2D(source, points, axis, true)
  }
  def apply(source: Image3D[_], points: vtkStructuredPoints, axis: Axis.Value) = new ImageActor2D(source, points, axis, false)
}

class ImageActor2D private[ImageActor2D](source: Image3D[_], points: vtkStructuredPoints, axis: Axis.Value, isStandalone: Boolean) extends PolyDataActor with ClickableActor {

  val OutOfBounds: Int = -1

  override def currentBoundingBox = VtkUtils.bounds2BoundingBox(points.GetBounds())

  lazy val (xmin , xmax, ymin, ymax, zmin, zmax) = {
    val b = points.GetBounds()
    (b(0),b(1),b(2),b(3),b(4),b(5))
  }
  lazy val (exmax, eymax, ezmax) = {
    val t = points.GetExtent()
    (t(1),t(3),t(5))
  }

  def point3DToExtent(p: Point3D, axis: Axis.Value) = {
    val (fmin, fmax, fval, tmax) = axis match {
      case Axis.X => (xmin, xmax, p.x, exmax)
      case Axis.Y => (ymin, ymax, p.y, eymax)
      case Axis.Z => (zmin, zmax, p.z, ezmax)
    }
    val idx = if (fval < fmin || fval > fmax) OutOfBounds else {
      val (nval, nmax) = (fval - fmin, fmax - fmin)
      Math.floor(nval * tmax / nmax).toInt
    }
    idx
  }


  val slice = new vtkImageDataGeometryFilter
  slice.SetInputData(points)
  slice.ThresholdValueOff()
  slice.ThresholdCellsOff()
  slice.SetExtent(0,exmax,0,eymax,0,ezmax)
  slice.Update()
  mapper.SetInputData(slice.GetOutput())

  val grayscale = new vtkLookupTable

  {
    val r = slice.GetOutput().GetScalarRange()
    mapper.SetScalarRange(r(0), r(1))

    grayscale.SetTableRange(r(0), r(1))
    grayscale.SetSaturationRange(0, 0)
    grayscale.SetHueRange(0, 0)
    grayscale.SetValueRange(0, 1)
    grayscale.Build()
    mapper.SetLookupTable(grayscale)
  }

  //mapper.SetScalarVisibility(0)
  update(source.scene.slicingPosition.point)

  def update(point: Point3D) = {
    val i = point3DToExtent(point, axis)
    if (i == OutOfBounds) {
      SetVisibility(0)
    } else {
      SetVisibility(1)
      axis match {
        case Axis.X => slice.SetExtent(i,i,0,eymax,0,ezmax)
        case Axis.Y => slice.SetExtent(0,exmax,i,i,0,ezmax)
        case Axis.Z => slice.SetExtent(0,exmax,0,eymax,i,i)
      }
      slice.Update()
      mapper.Update()
    }
    publishEdt(if (isStandalone) new ResetCameraRequest(this) else new RenderRequest(this))
  }

  listenTo(source.scene)

  reactions += {
    case Scene.SlicingPosition.PointChanged(sp) => update(sp.point)
  }

  override def onDestroy() {
    deafTo(source.scene)
    super.onDestroy()
    slice.Delete()
    grayscale.Delete()
    if (isStandalone) {
      points.Delete()
    }
  }

  override def clicked(point: Point3D) = source.addLandmarkAt(point)
}