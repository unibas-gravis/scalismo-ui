package scalismo.ui.rendering.internal

import java.awt.{ Component, Point }

import scalismo.geometry.{ Point3D, _3D }
import scalismo.ui.model.{ Axis, SceneNode }
import scalismo.ui.rendering.RendererState
import scalismo.ui.rendering.RendererState.PointAndNode
import scalismo.ui.rendering.actor.mixin.{ ActorLineWidth, ActorSceneNode }
import scalismo.ui.rendering.internal.CoordinateAdapter.VtkPoint
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D }
import vtk._

import scala.collection.immutable

class RendererStateImplementation(renderer: vtkRenderer, viewport: ViewportPanel) extends RendererState {
  val adapter = new CoordinateAdapter

  val cellPicker = new vtkCellPicker {
    PickFromListOff()
    // This is needed so that 2D sliced actors can also be picked.
    // (At the default tolerance of 1e-6, it's virtually impossible to
    // hit a 2D line). This might need a bit more tweaking.
    SetTolerance(.005)
  }

  lazy val axis: Option[Axis] = viewport match {
    case _2d: ViewportPanel2D => Some(_2d.axis)
    case _ => None
  }

  def setSize(width: Int, height: Int, panel: Component) = {
    adapter.setSize(width, height, panel)
  }

  override def isHighlightable(node: SceneNode): Boolean = {
    findHighlightable(node).nonEmpty
  }

  override def setHighlighted(node: SceneNode, onOff: Boolean): Unit = {
    findHighlightable(node).foreach { actor =>
      val add = if (onOff) 1 else 0
      actor.GetProperty().SetLineWidth(actor.lineWidth.value + add)
      viewport.rendererPanel.render()
    }
  }

  def findHighlightable(node: SceneNode): Option[ActorSceneNode with ActorLineWidth] = {
    val actors = renderer.GetActors()
    val count = actors.GetNumberOfItems()
    if (count > 1) {
      actors.InitTraversal()
      (0 until count) foreach { actor =>
        actors.GetNextActor() match {
          case ok: ActorSceneNode with ActorLineWidth if ok.sceneNode == node => return Some(ok)
          case _ =>
        }
      }
    }
    None
  }

  override def pointAndNodeAtPosition(point: Point): PointAndNode = {
    val pointAndProp = findPointAndPropAtPosition(adapter.toVtkPoint(point))
    PointAndNode(pointAndProp.point, pointAndProp.prop.flatMap(propToNode))
  }

  def propToNode(prop: vtkProp3D): Option[SceneNode] = {
    prop match {
      case n: ActorSceneNode => Some(n.sceneNode)
      case _ => None
    }
  }

  private case class PointAndProp(point: Option[Point3D], prop: Option[vtkProp3D])

  private def findPointAndPropAtPosition(vtkPoint: VtkPoint): PointAndProp = {
    if (cellPicker.Pick(vtkPoint.x, vtkPoint.y, 0.0, renderer) == 1) {
      // simple case
      val prop = cellPicker.GetProp3D()
      val array = cellPicker.GetPickPosition()
      val point: Option[Point3D] = if (array == null) None else Some(scalismo.geometry.Point[_3D](array.map(_.toFloat)))
      PointAndProp(point, Option(prop))
    } else {
      // complicated case
      val coord = new vtkCoordinate
      coord.SetValue(vtkPoint.x, vtkPoint.y, 0.0)
      coord.SetCoordinateSystemToDisplay()
      val array = coord.GetComputedWorldValue(renderer).map(_.toFloat)

      // these coordinates are necessarily inaccurate, because no object was hit, so
      // it's unclear where in space the point is located (in terms of "depth", i.e., how far away).
      // in 3D, there's nothing we can do about this, but in 2D, we know where the slice is, and
      // modify the point accordingly.
      axis match {
        case Some(Axis.X) => array(0) = viewport.frame.sceneControl.slicingPosition.x
        case Some(Axis.Y) => array(1) = viewport.frame.sceneControl.slicingPosition.y
        case Some(Axis.Z) => array(2) = viewport.frame.sceneControl.slicingPosition.z
        case _ => //
      }
      // return None if the point found is not within the bounding box of the scene
      val pointOption = {
        val point: Point3D = scalismo.geometry.Point[_3D](array)
        val bb = viewport.frame.sceneControl.slicingPosition.boundingBox
        if (bb.contains(point)) Some(point) else None
      }
      PointAndProp(pointOption, None)
    }
  }

}
