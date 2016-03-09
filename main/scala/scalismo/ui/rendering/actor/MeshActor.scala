package scalismo.ui.rendering.actor

import scalismo.geometry.{ _3D, Point }
import scalismo.ui.model.TriangleMeshNode
import scalismo.ui.model.properties.{ OpacityProperty, ColorProperty }
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.actor.mixin.{ ActorOpacity, ActorColor }
import scalismo.ui.view.{ ViewportPanel3D, ViewportPanel }
import scalismo.utils.MeshConversion
import vtk.vtkPolyData

object TriangleMeshActor extends SimpleActorsFactory[TriangleMeshNode] {

  override def actorsFor(renderable: TriangleMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new TriangleMeshActor3D(renderable))
      case _ => None
    }
  }
}

trait MeshActor extends PolyDataActor with ActorOpacity {
  def renderable: TriangleMeshNode

  override lazy val opacity: OpacityProperty = renderable.opacity

  protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData

  protected var polydata: vtkPolyData = meshToPolyData(None)

  protected def onGeometryChanged(): Unit

  protected def rerender(geometryChanged: Boolean = false, canUseTemplate: Boolean = true) = {
    if (geometryChanged) {
      val template = if (canUseTemplate) Some(polydata) else None
      polydata = meshToPolyData(template)
      onGeometryChanged()
    }

    //publishEdt(VtkContext.RenderRequest(this))
  }

  protected def onInstantiated(): Unit = {}

  //  reactions += {
  //    case MeshView.GeometryChanged(m) => rerender(geometryChanged = true)
  //    case MeshView.Reloaded(m) => rerender(geometryChanged = true, canUseTemplate = false)
  //  }

  listenTo(renderable)

  onInstantiated()

  rerender(geometryChanged = true)
}

abstract class MeshActor3D(override val renderable: TriangleMeshNode) extends MeshActor {

  // not declaring this as lazy causes all sorts of weird VTK errors, probably because the methods which use
  // it are invoked from the superclass constructor (at which time this class is not necessarily fully initialized)(?)
  lazy val normals = new vtk.vtkPolyDataNormals() {
    ComputePointNormalsOn()
    ComputeCellNormalsOff()
  }

  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(normals.GetOutputPort())
  }

  override protected def onGeometryChanged() = {
    normals.RemoveAllInputs()
    normals.SetInputData(polydata)
    normals.Update()
  }

}

trait TriangleMeshActor extends MeshActor with ActorColor {
  //override def renderable: TriangleMeshRenderable

  override def color: ColorProperty = renderable.color

  override protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData = {
    Caches.TriangleMeshCache.getOrCreate(renderable.mesh, MeshConversion.meshToVtkPolyData(renderable.mesh, template))
  }

}

class TriangleMeshActor3D(override val renderable: TriangleMeshNode) extends MeshActor3D(renderable) with TriangleMeshActor {

  //class TriangleMeshActor3D(source: TriangleMeshNode) extends PolyDataActor with ActorColor {
  //  override def color: ColorProperty = source.color
  //
  //
}
