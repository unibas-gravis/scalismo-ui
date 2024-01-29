/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.rendering.actor

import scalismo.geometry.Point3D
import scalismo.ui.control.SlicingPosition
import scalismo.ui.model.properties.{NodeProperty, OpacityProperty}
import scalismo.ui.model.{Axis, BoundingBox, ImageNode}
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.actor.ImageActor2D.InstanceData
import scalismo.ui.rendering.actor.mixin.{ActorOpacity, ActorSceneNode, IsImageActor}
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ScalismoFrame, ViewportPanel, ViewportPanel2D, ViewportPanel3D}
import scalismo.vtk.utils.ImageConversion
import vtk._

object ImageActor extends SimpleActorsFactory[ImageNode] {
  override def actorsFor(renderable: ImageNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _2d: ViewportPanel2D => Some(ImageActor2D(renderable, _2d))
      case _3d: ViewportPanel3D => Some(new ImageActor3D(renderable, _3d))
    }
  }
}

object ImageActor2D {

  def apply(node: ImageNode, viewport: ViewportPanel2D): ImageActor2D with SingleDataSetActor =
    new ImageActor2D(node, viewport.axis, viewport.frame) with SingleDataSetActor {
      override def boundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(data.points.GetBounds())
    }

  def apply(node: ImageNode, axis: Axis, frame: ScalismoFrame) = new ImageActor2D(node, axis, frame)

  final val OutOfBounds: Int = -1
  final val NotInitialized: Int = -2

  class InstanceData(node: ImageNode, axis: Axis) {
    val points: vtkStructuredPoints =
      Caches.ImageCache.getOrCreate(node.source, ImageConversion.imageToVtkStructuredPoints(node.source))
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
    windowLevel.SetWindow(node.windowLevel.value.window)
    windowLevel.SetLevel(node.windowLevel.value.level)
    windowLevel.Update()
    windowLevel.SetOutputFormatToLuminance()

    val slice = new vtkImageDataGeometryFilter
    slice.SetInputConnection(windowLevel.GetOutputPort())
    slice.ThresholdValueOff()
    slice.ThresholdCellsOff()
    slice.SetExtent(0, 0, 0, 0, 0, 0)
    slice.Update()

    // Transform used to correct image slice, such that
    // it coincides with the slicing position (which is in general not the case as the slicer slices only
    // at the grid position).
    val slicePositionCorrector = new vtkTransformPolyDataFilter()
    val sliceCorrectionTransform = new vtkTransform()
    sliceCorrectionTransform.Translate(0, 0, 0)
    slicePositionCorrector.SetTransform(sliceCorrectionTransform)
    slicePositionCorrector.SetInputConnection(slice.GetOutputPort())

  }

}

class ImageActor2D private[ImageActor2D] (override val sceneNode: ImageNode, axis: Axis, frame: ScalismoFrame)
    extends DataSetActor
    with IsImageActor
    with ActorOpacity
    with ActorEvents
    with ActorSceneNode {

  override def opacity: OpacityProperty = sceneNode.opacity

  val data = new InstanceData(sceneNode, axis)

  // This method computes the closest into the image for the given slicing position (point) for a given axis.
  def point3DToSliceIndex(p: Point3D, axis: Axis): Int = {
    val (fmin, fmax, fval, tmax) = axis match {
      case Axis.X => (data.min, data.max, p.x, data.exmax)
      case Axis.Y => (data.min, data.max, p.y, data.eymax)
      case Axis.Z => (data.min, data.max, p.z, data.ezmax)
    }

    if (fval < fmin || fval > fmax) ImageActor2D.OutOfBounds
    else {
      val (nval, nmax) = (fval - fmin, fmax - fmin)
      val idx = Math.round(nval * tmax / nmax).toInt
      idx
    }

  }

  def update(slicingPoint: Point3D, geometryChanged: Boolean): Unit = {
    val sliceIndex = point3DToSliceIndex(slicingPoint, axis)

    if (sliceIndex == ImageActor2D.OutOfBounds) {
      SetVisibility(0)
    } else {
      SetVisibility(1)

      // since the vtkImageDataGeometryFilter that is currently used for slicing does only
      // support slicing at grid position, the slicing position is in general different from the
      // indicated slicing position. We correct this by computing an additional translation.
      val (origin, spacing) = (data.points.GetOrigin(), data.points.GetSpacing())
      data.sliceCorrectionTransform.Identity()

      def computeOffset(component: Int): Double = {
        val pointComponentForIndex = origin(component) + sliceIndex * spacing(component)
        val offset = slicingPoint(component) - pointComponentForIndex
        offset
      }

      axis match {
        case Axis.X =>
          data.slice.SetExtent(sliceIndex, sliceIndex, 0, data.eymax, 0, data.ezmax)
          val offset = computeOffset(0)
          data.sliceCorrectionTransform.Translate(+offset, 0, 0)
        case Axis.Y =>
          data.slice.SetExtent(0, data.exmax, sliceIndex, sliceIndex, 0, data.ezmax)
          val offset = computeOffset(1)
          data.sliceCorrectionTransform.Translate(0, +offset, 0)
        case Axis.Z =>
          data.slice.SetExtent(0, data.exmax, 0, data.eymax, sliceIndex, sliceIndex)
          val offset = computeOffset(2)
          data.sliceCorrectionTransform.Translate(0, 0, +offset)
      }
      data.sliceCorrectionTransform.Modified()
      data.slicePositionCorrector.Modified()
      data.slice.Modified()
      mapper.Modified()
    }
    actorChanged(geometryChanged)

  }

  def updateWindowLevel(): Unit = {
    val wl = sceneNode.windowLevel.value
    if (data.windowLevel.GetWindow() != wl.window || data.windowLevel.GetLevel() != wl.level) {
      data.windowLevel.SetWindow(wl.window)
      data.windowLevel.SetLevel(wl.level)
      data.windowLevel.Modified()
      actorChanged()
    }
  }

  listenTo(frame.sceneControl.slicingPosition, sceneNode.windowLevel)

  mapper.SetInputConnection(data.slicePositionCorrector.GetOutputPort())
  update(frame.sceneControl.slicingPosition.point, geometryChanged = true)

  reactions += {
    case SlicingPosition.event.PointChanged(_, _, current) => update(current, geometryChanged = false)
    case NodeProperty.event.PropertyChanged(_)             => updateWindowLevel()
  }
}

class ImageActor3D(node: ImageNode, viewport: ViewportPanel3D) extends Actors {
  override val vtkActors: List[ImageActor2D] = Axis.All.map { axis =>
    ImageActor2D(node, axis, viewport.frame)
  }

  // the actors all return the same bounding box, so we just take the first
  override def boundingBox: BoundingBox =
    vtkActors.headOption.map(a => VtkUtil.bounds2BoundingBox(a.data.points.GetBounds())).getOrElse(BoundingBox.Invalid)
}
