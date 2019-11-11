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

import scalismo.geometry._3D
import scalismo.mesh.{LineMesh, ScalarMeshField, TriangleMesh, VertexColorMesh3D}
import scalismo.tetramesh.TetrahedralMesh
import scalismo.ui.model._
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.Caches.{FastCachingTetrahedralMesh, FastCachingTriangleMesh, FastCachingVertexColorMesh}
import scalismo.ui.rendering.actor.MeshActor.MeshRenderable
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ViewportPanel, ViewportPanel2D, ViewportPanel3D}
import scalismo.utils.{MeshConversion, TetraMeshConversion}
import vtk.{vtkPolyData, vtkPolyDataNormals, vtkUnsignedCharArray, vtkUnstructuredGrid}


object TriangleMeshActor extends SimpleActorsFactory[TriangleMeshNode] {

  override def actorsFor(renderable: TriangleMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new TriangleMeshActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new TriangleMeshActor2D(renderable, _2d))
    }
  }
}

object TetrahedralMeshActor extends SimpleActorsFactory[TetrahedralMeshNode] {

  override def actorsFor(renderable: TetrahedralMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new TetrahedralMeshActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new TetrahedralMeshActor2D(renderable, _2d))
    }
  }
}

//object TetrahedralMeshFieldActor extends SimpleActorsFactory[TetrahedralMeshFieldNode] {
//
//  override def actorsFor(renderable: TetrahedralMeshFieldNode, viewport: ViewportPanel): Option[Actors] = {
//    viewport match {
//      case _: ViewportPanel3D => Some(new TetrahedralMeshFieldActor3D(renderable))
//      case _2d: ViewportPanel2D => Some(new TetrahedralMeshFieldActor2D(renderable, _2d))
//    }
//  }
//}


object ScalarMeshFieldActor extends SimpleActorsFactory[ScalarMeshFieldNode] {

  override def actorsFor(renderable: ScalarMeshFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new ScalarMeshFieldActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new ScalarMeshFieldActor2D(renderable, _2d))
    }
  }
}

object LineMeshActor extends SimpleActorsFactory[LineMeshNode] {
  override def actorsFor(renderable: LineMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new LineMeshActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new LineMeshActor2D(renderable, _2d))
    }
  }
}

object VertexColorMeshActor extends SimpleActorsFactory[VertexColorMeshNode] {
  override def actorsFor(renderable: VertexColorMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new VertexColorMeshActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new VertexColorMeshActor2D(renderable, _2d))
    }
  }
}

object MeshActor {

  trait MeshRenderable {

    type MeshType

    def opacity: OpacityProperty

    def lineWidth: LineWidthProperty

    def mesh: MeshType

    def node: SceneNode
  }

  private[actor] object MeshRenderable {

    class TriangleMeshRenderable(override val node: TriangleMeshNode) extends MeshRenderable {

      type MeshType = TriangleMesh[_3D]

      override def opacity: OpacityProperty = node.opacity

      override def mesh: TriangleMesh[_3D] = node.transformedSource

      override def lineWidth: LineWidthProperty = node.lineWidth

      def color: ColorProperty = node.color
    }

    class TetrahedralMeshRenderable(override val node: TetrahedralMeshNode) extends MeshRenderable {

      type MeshType = TetrahedralMesh[_3D]

      override def opacity: OpacityProperty = node.opacity

      override def mesh: TetrahedralMesh[_3D] = node.transformedSource

      override def lineWidth: LineWidthProperty = node.lineWidth

      def color: ColorProperty = node.color
    }

    class VertexColorMeshRenderable(override val node: VertexColorMeshNode) extends MeshRenderable {

      type MeshType = TriangleMesh[_3D]

      override def mesh: TriangleMesh[_3D] = node.transformedSource.shape

      override def opacity: OpacityProperty = node.opacity

      override def lineWidth: LineWidthProperty = node.lineWidth

      def colorMesh: VertexColorMesh3D = node.transformedSource
    }

    class ScalarMeshFieldRenderable(override val node: ScalarMeshFieldNode) extends MeshRenderable {

      type MeshType = TriangleMesh[_3D]

      override def opacity: OpacityProperty = node.opacity

      override def lineWidth: LineWidthProperty = node.lineWidth

      override def mesh: TriangleMesh[_3D] = field.mesh

      def scalarRange: ScalarRangeProperty = node.scalarRange

      def field: ScalarMeshField[Float] = node.transformedSource
    }

    class LineMeshRenderable(override val node: LineMeshNode) extends MeshRenderable {
      type MeshType = LineMesh[_3D]

      override def opacity: OpacityProperty = node.opacity

      override def lineWidth: LineWidthProperty = node.lineWidth

      override def mesh: LineMesh[_3D] = node.transformedSource

      def color: ColorProperty = node.color

    }

    def apply(source: TriangleMeshNode): TriangleMeshRenderable = new TriangleMeshRenderable(source)

    def apply(source: VertexColorMeshNode): VertexColorMeshRenderable = new VertexColorMeshRenderable(source)

    def apply(source: ScalarMeshFieldNode): ScalarMeshFieldRenderable = new ScalarMeshFieldRenderable(source)

    def apply(source: LineMeshNode): LineMeshRenderable = new LineMeshRenderable(source)

    def apply(source: TetrahedralMeshNode): TetrahedralMeshRenderable = new TetrahedralMeshRenderable(source)

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

  protected def rerender(geometryChanged: Boolean): Unit = {
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




trait TetrahedralMeshActor extends MeshActor[MeshRenderable.TetrahedralMeshRenderable] with ActorColor {
  override def renderable: MeshRenderable.TetrahedralMeshRenderable

  override def color: ColorProperty = renderable.color

  override protected def meshToPolyData(template2: Option[vtkPolyData]): vtkPolyData = {

    def TetrahedralMeshToVtkPolyData(data: TetrahedralMesh[_3D], template2: Option[vtkUnstructuredGrid]): vtkPolyData = {
      val t = new vtk.vtkDataSetSurfaceFilter()
      val unstructuredgrid = TetraMeshConversion.tetrameshTovtkUnstructuredGrid(renderable.mesh, template2)
      t.AddInputData(unstructuredgrid)
      t.Update()
      val polydata: vtkPolyData = t.GetOutput()
      polydata
    }

    Caches.TetrahedralMeshCache.getOrCreate(FastCachingTetrahedralMesh(renderable.mesh), TetrahedralMeshToVtkPolyData(renderable.mesh, None))

  }

}



trait VertexColorMeshActor extends MeshActor[MeshRenderable.VertexColorMeshRenderable] {

  override def renderable: MeshRenderable.VertexColorMeshRenderable

  override protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData = {

    def colorMeshToVtkPd(colorMesh: VertexColorMesh3D): vtkPolyData = {

      val pd = MeshConversion.meshToVtkPolyData(colorMesh.shape)
      val vtkColors = new vtkUnsignedCharArray()
      vtkColors.SetNumberOfComponents(3)
      vtkColors.SetName("RGB")

      for (id <- colorMesh.shape.pointSet.pointIds) {
        val color = colorMesh.color(id)
        vtkColors.InsertNextTuple3((color.r * 255).toShort, (color.g * 255).toShort, (color.b * 255).toShort)
      }
      pd.GetPointData().SetScalars(vtkColors)
      pd
    }

    Caches.VertexColorMeshCache.getOrCreate(FastCachingVertexColorMesh(renderable.colorMesh), colorMeshToVtkPd(renderable.colorMesh))
  }
}

trait LineMeshActor extends MeshActor[MeshRenderable.LineMeshRenderable] with ActorColor with ActorLineWidth {
  override def renderable: MeshRenderable.LineMeshRenderable

  override def color: ColorProperty = renderable.color

  override protected def meshToPolyData(template: Option[vtkPolyData]): vtkPolyData = {

    MeshConversion.lineMeshToVTKPolyData(renderable.mesh, template)
  }

  override def lineWidth: LineWidthProperty = renderable.lineWidth

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
  private lazy val normals: vtkPolyDataNormals = new vtk.vtkPolyDataNormals() {
    ComputePointNormalsOn()
    ComputeCellNormalsOff()
  }

  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(normals.GetOutputPort())
  }

  override protected def onGeometryChanged(): Unit = {
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

class VertexColorMeshActor3D(node: VertexColorMeshNode) extends MeshActor3D(MeshRenderable(node)) with VertexColorMeshActor

class VertexColorMeshActor2D(node: VertexColorMeshNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with VertexColorMeshActor

class ScalarMeshFieldActor3D(node: ScalarMeshFieldNode) extends MeshActor3D(MeshRenderable(node)) with ScalarMeshFieldActor

class ScalarMeshFieldActor2D(node: ScalarMeshFieldNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with ScalarMeshFieldActor

class LineMeshActor3D(node: LineMeshNode) extends MeshActor3D(MeshRenderable(node)) with LineMeshActor

class LineMeshActor2D(node: LineMeshNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with LineMeshActor

class TetrahedralMeshActor3D(node: TetrahedralMeshNode) extends MeshActor3D(MeshRenderable(node)) with TetrahedralMeshActor

class TetrahedralMeshActor2D(node: TetrahedralMeshNode, viewport: ViewportPanel2D) extends MeshActor2D(MeshRenderable(node), viewport) with TetrahedralMeshActor