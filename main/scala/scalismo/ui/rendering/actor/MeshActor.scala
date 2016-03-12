package scalismo.ui.rendering.actor

import scalismo.mesh.TriangleMesh
import scalismo.ui.model.properties.{ ColorProperty, LineWidthProperty, OpacityProperty, ScalarRangeProperty }
import scalismo.ui.model.{ BoundingBox, ScalarMeshFieldNode, TriangleMeshNode }
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.actor.MeshActor.MeshRenderable
import scalismo.ui.rendering.actor.mixin.{ ActorColor, ActorLineWidth, ActorOpacity, ActorScalarRange }
import scalismo.ui.rendering.util.BoundingBoxUtil
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

    def mesh: TriangleMesh
  }

  private[actor] object MeshRenderable {

    class TriangleMeshRenderable(node: TriangleMeshNode) extends MeshRenderable {
      override def opacity = node.opacity

      override def mesh = node.source

      override def lineWidth = node.lineWidth

      def color = node.color
    }

    class ScalarMeshFieldRenderable(node: ScalarMeshFieldNode) extends MeshRenderable {
      override def opacity = node.opacity

      override def lineWidth = node.lineWidth

      override def mesh = node.source.mesh

      def scalarRange = node.scalarRange

      def field = node.source
    }

    def apply(source: TriangleMeshNode): TriangleMeshRenderable = new TriangleMeshRenderable(source)

    def apply(source: ScalarMeshFieldNode): ScalarMeshFieldRenderable = new ScalarMeshFieldRenderable(source)

  }

}

trait MeshActor[R <: MeshRenderable] extends SinglePolyDataActor with ActorOpacity {
  def renderable: R

  override def opacity: OpacityProperty = renderable.opacity

  protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData

  protected var polydata: vtkPolyData = meshToPolyData(None)

  protected def onGeometryChanged(): Unit

  protected def rerender(geometryChanged: Boolean = false, canUseTemplate: Boolean = true) = {
    if (geometryChanged) {
      val template = if (canUseTemplate) Some(polydata) else None
      polydata = meshToPolyData(template)
      onGeometryChanged()
    }
    requestRendering()
  }

  protected def onInstantiated(): Unit = {}

  //  reactions += {
  //    case MeshView.GeometryChanged(m) => rerender(geometryChanged = true)
  //    case MeshView.Reloaded(m) => rerender(geometryChanged = true, canUseTemplate = false)
  //  }

  //listenTo(renderable)

  onInstantiated()

  rerender(geometryChanged = true)
}

trait TriangleMeshActor extends MeshActor[MeshRenderable.TriangleMeshRenderable] with ActorColor {
  override def renderable: MeshRenderable.TriangleMeshRenderable

  override def color: ColorProperty = renderable.color

  override protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData = {
    Caches.TriangleMeshCache.getOrCreate(renderable.mesh, MeshConversion.meshToVtkPolyData(renderable.mesh, template))
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

  override protected def onSlicingPositionChanged(): Unit = rerender()

  override protected def onGeometryChanged(): Unit = {
    planeCutter.SetInputData(polydata)
    planeCutter.Modified()
  }

  override protected def sourceBoundingBox: BoundingBox = BoundingBoxUtil.bounds2BoundingBox(polydata.GetBounds())
}

class TriangleMeshActor3D(node: TriangleMeshNode) extends MeshActor3D(MeshRenderable(node)) with TriangleMeshActor

class TriangleMeshActor2D(node: TriangleMeshNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with TriangleMeshActor

class ScalarMeshFieldActor3D(node: ScalarMeshFieldNode) extends MeshActor3D(MeshRenderable(node)) with ScalarMeshFieldActor

class ScalarMeshFieldActor2D(node: ScalarMeshFieldNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with ScalarMeshFieldActor