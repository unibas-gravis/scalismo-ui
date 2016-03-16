package scalismo.ui.rendering.actor

import scalismo.geometry.Point3D
import scalismo.ui.control.SlicingPosition
import scalismo.ui.model.properties.NodeProperty
import scalismo.ui.model.{ Axis, BoundingBox, ImageNode }
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.actor.ImageActor2D.InstanceData
import scalismo.ui.rendering.actor.mixin.{ ActorOpacity, ActorSceneNode }
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import scalismo.utils.ImageConversion
import vtk.{ vtkImageDataGeometryFilter, vtkImageMapToWindowLevelColors, vtkStructuredPoints }

object ImageActor extends SimpleActorsFactory[ImageNode] {
  override def actorsFor(renderable: ImageNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _2d: ViewportPanel2D => Some(ImageActor2D(renderable, _2d))
      case _3d: ViewportPanel3D => Some(new ImageActor3D(renderable, _3d))
    }
  }
}

object ImageActor2D {

  def apply(node: ImageNode, viewport: ViewportPanel2D) = new ImageActor2D(node, viewport.axis, viewport.frame) with SinglePolyDataActor {
    override def boundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(data.points.GetBounds())
  }

  def apply(node: ImageNode, axis: Axis, frame: ScalismoFrame) = new ImageActor2D(node, axis, frame)

  final val OutOfBounds: Int = -1
  final val NotInitialized: Int = -2

  class InstanceData(node: ImageNode, axis: Axis) {
    val points: vtkStructuredPoints = Caches.ImageCache.getOrCreate(node.source, ImageConversion.imageToVtkStructuredPoints(node.source))
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

  }

}

class ImageActor2D private[ImageActor2D] (override val sceneNode: ImageNode, axis: Axis, frame: ScalismoFrame) extends PolyDataActor with ActorOpacity with ActorEvents with ActorSceneNode {

  override def opacity = sceneNode.opacity

  val data = new InstanceData(sceneNode, axis)

  var currentIndex = ImageActor2D.NotInitialized

  def point3DToExtent(p: Point3D, axis: Axis) = {
    val (fmin, fmax, fval, tmax) = axis match {
      case Axis.X => (data.min, data.max, p.x, data.exmax)
      case Axis.Y => (data.min, data.max, p.y, data.eymax)
      case Axis.Z => (data.min, data.max, p.z, data.ezmax)
    }
    val idx = if (fval < fmin || fval > fmax) ImageActor2D.OutOfBounds
    else {
      val (nval, nmax) = (fval - fmin, fmax - fmin)
      Math.floor(nval * tmax / nmax).toInt
    }
    idx
  }

  def update(point: Point3D, geometryChanged: Boolean): Unit = {
    val i = point3DToExtent(point, axis)
    if (i != currentIndex) {
      currentIndex = i
      if (i == ImageActor2D.OutOfBounds) {
        SetVisibility(0)
      } else {
        SetVisibility(1)
        axis match {
          case Axis.X => data.slice.SetExtent(i, i, 0, data.eymax, 0, data.ezmax)
          case Axis.Y => data.slice.SetExtent(0, data.exmax, i, i, 0, data.ezmax)
          case Axis.Z => data.slice.SetExtent(0, data.exmax, 0, data.eymax, i, i)
        }
        data.slice.Modified()
      }
      actorChanged(geometryChanged)
    }
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

  mapper.SetInputConnection(data.slice.GetOutputPort())
  currentIndex = ImageActor2D.NotInitialized
  update(frame.sceneControl.slicingPosition.point, geometryChanged = true)

  reactions += {
    case SlicingPosition.event.PointChanged(_, _, current) => update(current, geometryChanged = false)
    case NodeProperty.event.PropertyChanged(_) => updateWindowLevel()
  }
}

class ImageActor3D(node: ImageNode, viewport: ViewportPanel3D) extends Actors {
  override def vtkActors: List[ImageActor2D] = Axis.All.map { axis => ImageActor2D(node, axis, viewport.frame) }

  // the actors all return the same bounding box, so we just take the first
  override def boundingBox: BoundingBox = vtkActors.headOption.map(a => VtkUtil.bounds2BoundingBox(a.data.points.GetBounds())).getOrElse(BoundingBox.Invalid)
}
