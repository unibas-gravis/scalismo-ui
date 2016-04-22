package scalismo.ui.rendering.actor

import scalismo.geometry._3D
import scalismo.mesh.{ ScalarMeshField, TriangleMesh }
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.model.{ BoundingBox, ScalarMeshFieldNode, SceneNode, TriangleMeshNode }
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.Caches.FastCachingTriangleMesh
import scalismo.ui.rendering.actor.MeshActor.MeshRenderable
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import scalismo.utils.MeshConversion
import vtk.vtkPolyData

object TriangleMeshActor extends SimpleActorsFactory[TriangleMeshNode] {

  override def actorsFor(renderable: TriangleMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new TriangleMeshActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new TriangleMeshActor2D(renderable, _2d))
    }
  }
}

object ScalarMeshFieldActor extends SimpleActorsFactory[ScalarMeshFieldNode] {

  override def actorsFor(renderable: ScalarMeshFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new ScalarMeshFieldActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new ScalarMeshFieldActor2D(renderable, _2d))
    }
  }
}

object MeshActor {

  trait MeshRenderable {
    def opacity: OpacityProperty

    def lineWidth: LineWidthProperty

    def mesh: TriangleMesh[_3D]

    def node: SceneNode
  }

  private[actor] object MeshRenderable {

    class TriangleMeshRenderable(override val node: TriangleMeshNode) extends MeshRenderable {
      override def opacity: OpacityProperty = node.opacity

      override def mesh: TriangleMesh[_3D] = node.transformedSource

      override def lineWidth: LineWidthProperty = node.lineWidth

      def color: ColorProperty = node.color
    }

    class ScalarMeshFieldRenderable(override val node: ScalarMeshFieldNode) extends MeshRenderable {
      override def opacity: OpacityProperty = node.opacity

      override def lineWidth: LineWidthProperty = node.lineWidth

      override def mesh: TriangleMesh[_3D] = field.mesh

      def scalarRange: ScalarRangeProperty = node.scalarRange

      def field: ScalarMeshField[Float] = node.transformedSource
    }

    def apply(source: TriangleMeshNode): TriangleMeshRenderable = new TriangleMeshRenderable(source)

    def apply(source: ScalarMeshFieldNode): ScalarMeshFieldRenderable = new ScalarMeshFieldRenderable(source)

  }

}

trait MeshActor[R <: MeshRenderable] extends SinglePolyDataActor with ActorOpacity with ActorSceneNode {
  def renderable: R

  override def opacity: OpacityProperty = renderable.opacity

  override def sceneNode: SceneNode = renderable.node

  protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData

  protected var polydata: vtkPolyData = meshToPolyData(None)

  // this is invoked from within the rerender method, if the geometry has changed.
  protected def onGeometryChanged(): Unit

  protected def rerender(geometryChanged: Boolean) = {
    if (geometryChanged) {
      polydata = meshToPolyData(Some(polydata))
      onGeometryChanged()
    }
    actorChanged(geometryChanged)
  }

  protected def onInstantiated(): Unit = {}

  //FIXME: pick control -- this should probably go into a trait or something.
  renderable.node match {
    case p: HasPickable =>
      SetPickable(if (p.pickable.value) 1 else 0)
      listenTo(p.pickable)
      reactions += {
        case NodeProperty.event.PropertyChanged(s) if s == p.pickable =>
          SetPickable(if (p.pickable.value) 1 else 0)
      }
    case _ =>
  }

  onInstantiated()

  rerender(geometryChanged = true)

  listenTo(renderable.node)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(geometryChanged = true)
  }

}

trait TriangleMeshActor extends MeshActor[MeshRenderable.TriangleMeshRenderable] with ActorColor {
  override def renderable: MeshRenderable.TriangleMeshRenderable

  override def color: ColorProperty = renderable.color

  override protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData = {

    Caches.TriangleMeshCache.getOrCreate(FastCachingTriangleMesh(renderable.mesh), MeshConversion.meshToVtkPolyData(renderable.mesh, template))
  }

}

trait ScalarMeshFieldActor extends MeshActor[MeshRenderable.ScalarMeshFieldRenderable] with ActorScalarRange {
  override def renderable: MeshRenderable.ScalarMeshFieldRenderable

  override def scalarRange: ScalarRangeProperty = renderable.scalarRange

  override protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData = {
    Caches.ScalarMeshFieldCache.getOrCreate(renderable.field, MeshConversion.scalarMeshFieldToVtkPolyData(renderable.field))
  }

}

abstract class MeshActor3D[R <: MeshRenderable](override val renderable: R) extends MeshActor[R] {

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

abstract class MeshActor2D[R <: MeshRenderable](override val renderable: R, viewport: ViewportPanel2D) extends SlicingActor(viewport) with MeshActor[R] with ActorLineWidth {
  override def lineWidth: LineWidthProperty = renderable.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(geometryChanged = false)

  override protected def onGeometryChanged(): Unit = {
    planeCutter.SetInputData(polydata)
    planeCutter.Modified()
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(polydata.GetBounds())
}

class TriangleMeshActor3D(node: TriangleMeshNode) extends MeshActor3D(MeshRenderable(node)) with TriangleMeshActor

class TriangleMeshActor2D(node: TriangleMeshNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with TriangleMeshActor

class ScalarMeshFieldActor3D(node: ScalarMeshFieldNode) extends MeshActor3D(MeshRenderable(node)) with ScalarMeshFieldActor

class ScalarMeshFieldActor2D(node: ScalarMeshFieldNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with ScalarMeshFieldActor