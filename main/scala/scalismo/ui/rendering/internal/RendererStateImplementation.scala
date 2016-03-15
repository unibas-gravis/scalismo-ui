package scalismo.ui.rendering.internal

import java.awt.{ Component, Point }

import scalismo.geometry.{ Point3D, _3D }
import scalismo.ui.model.{ SceneNode, Axis }
import scalismo.ui.rendering.RendererState
import scalismo.ui.rendering.RendererState.PointAndNode
import scalismo.ui.rendering.actor.mixin.ActorSceneNode
import scalismo.ui.rendering.internal.CoordinateAdapter.VtkPoint
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D }
import vtk.{ vtkCellPicker, vtkCoordinate, vtkProp3D, vtkRenderer }

class RendererStateImplementation(renderer: vtkRenderer, viewport: ViewportPanel) extends RendererState {
  val adapter = new CoordinateAdapter

  val cellPicker = new vtkCellPicker {
    PickFromListOff()
  }

  lazy val axis: Option[Axis] = viewport match {
    case _2d: ViewportPanel2D => Some(_2d.axis)
    case _ => None
  }

  def setSize(width: Int, height: Int, panel: Component) = {
    adapter.setSize(width, height, panel)
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
      }
      // return None if the point found is not within the bounding box of the scene
      //      val point = viewportOption.flatMap { vp =>
      //        val point = Point[_3D](array)
      //        val bb = vp.scene.slicingPosition.boundingBox
      //        if (bb.contains(point)) Some(point) else None
      //      }
      PointAndProp(Some(scalismo.geometry.Point[_3D](array)), None)
    }
  }

}
