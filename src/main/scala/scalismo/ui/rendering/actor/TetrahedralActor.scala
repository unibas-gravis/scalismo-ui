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

import scalismo.common.Scalar
import scalismo.geometry._3D
import scalismo.tetramesh.{ ScalarVolumeMeshField, TetrahedralMesh }
import scalismo.ui.model._
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.rendering.Caches
import scalismo.ui.rendering.Caches.{ FastCachingTetrahedralMesh, FastCachingTetrahedralMeshField }
import scalismo.ui.rendering.actor.TetrahedralActor.TetrahedralRenderable
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import scalismo.utils.{ TetraMeshConversion, VtkHelpers }
import vtk.vtkUnstructuredGrid

import scala.reflect.runtime.universe.TypeTag
import scala.reflect.ClassTag

object TetrahedralMeshActor extends SimpleActorsFactory[TetrahedralMeshNode] {

  override def actorsFor(renderable: TetrahedralMeshNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new TetrahedralMeshActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new TetrahedralMeshActor2D(renderable, _2d))
    }
  }
}

object ScalarTetrahedralMeshFieldActor extends SimpleActorsFactory[ScalarTetrahedralMeshFieldNode] {

  override def actorsFor(renderable: ScalarTetrahedralMeshFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new ScalarTetrahedralMeshFieldActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new ScalarTetrahedralMeshFieldActor2D(renderable, _2d))
    }
  }
}

object TetrahedralActor {

  trait TetrahedralRenderable {

    type MeshType

    def opacity: OpacityProperty

    def lineWidth: LineWidthProperty

    def mesh: MeshType

    def node: SceneNode
  }

  private[actor] object TetrahedralRenderable {

    class TetrahedralMeshRenderable(override val node: TetrahedralMeshNode) extends TetrahedralRenderable {

      type MeshType = TetrahedralMesh[_3D]

      override def mesh: TetrahedralMesh[_3D] = node.transformedSource

      override def opacity: OpacityProperty = node.opacity

      override def lineWidth: LineWidthProperty = node.lineWidth

      def color: ColorProperty = node.color
    }

    class ScalarTetrahedralMeshFieldRenderable(override val node: ScalarTetrahedralMeshFieldNode) extends TetrahedralRenderable {

      type MeshType = TetrahedralMesh[_3D]

      override def opacity: OpacityProperty = node.opacity

      override def lineWidth: LineWidthProperty = node.lineWidth

      override def mesh: TetrahedralMesh[_3D] = field.mesh

      def scalarRange: ScalarRangeProperty = node.scalarRange

      def field: ScalarVolumeMeshField[Float] = node.transformedSource
    }

    def apply(source: TetrahedralMeshNode): TetrahedralMeshRenderable = new TetrahedralMeshRenderable(source)

    def apply(source: ScalarTetrahedralMeshFieldNode): ScalarTetrahedralMeshFieldRenderable = new ScalarTetrahedralMeshFieldRenderable(source)
  }

}

trait TetrahedralActor[R <: TetrahedralRenderable] extends SingleDataSetActor with ActorOpacity with ActorSceneNode {
  def renderable: R

  override def opacity: OpacityProperty = renderable.opacity

  override def sceneNode: SceneNode = renderable.node

  protected def meshToUnstructuredGrid(template: Option[vtkUnstructuredGrid]): vtkUnstructuredGrid

  protected var unstructuredgrid: vtkUnstructuredGrid = meshToUnstructuredGrid(None)

  // this is invoked from within the rerender method, if the geometry has changed.
  protected def onGeometryChanged(): Unit

  protected def rerender(geometryChanged: Boolean): Unit = {
    if (geometryChanged) {
      unstructuredgrid = meshToUnstructuredGrid(None)
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

trait TetrahedralMeshActor extends TetrahedralActor[TetrahedralRenderable.TetrahedralMeshRenderable] {

  override def renderable: TetrahedralRenderable.TetrahedralMeshRenderable

  override protected def meshToUnstructuredGrid(template: Option[vtkUnstructuredGrid]): vtkUnstructuredGrid = {
    Caches.TetrahedralMeshCache.getOrCreate(FastCachingTetrahedralMesh(renderable.mesh), TetraMeshConversion.tetrameshTovtkUnstructuredGrid(renderable.mesh))
  }
}

trait TetrahedralMeshScalarFieldActor extends TetrahedralActor[TetrahedralRenderable.ScalarTetrahedralMeshFieldRenderable] with ActorScalarRange {

  override def renderable: TetrahedralRenderable.ScalarTetrahedralMeshFieldRenderable
  override def scalarRange: ScalarRangeProperty = renderable.scalarRange

  //lazy val grid: vtkUnstructuredGrid = TetraMeshConversion.tetrameshTovtkUnstructuredGrid(renderable.mesh) //using grid instead of unstructured grid, because unstructuredgrid defined above as protected var in a trait

  //unstructuredgrid = TetraMeshConversion.tetrameshTovtkUnstructuredGrid(renderable.mesh)

  def scalarVolumeMeshFieldToVtkUnstructuredGrid[S: Scalar: ClassTag: TypeTag](tetraMeshData: ScalarVolumeMeshField[S]): vtkUnstructuredGrid = { //note copied from Conversions.scalarArrayToVtkDataArray
    val scalarData = VtkHelpers.scalarArrayToVtkDataArray(tetraMeshData.data, 1)
    unstructuredgrid.GetPointData().SetScalars(scalarData) //pointDataArrayVTK)
    //    unstructuredgrid = grid
    unstructuredgrid
  }

  override protected def meshToUnstructuredGrid(template: Option[vtkUnstructuredGrid]): vtkUnstructuredGrid = {
    if (template.isEmpty) {
      unstructuredgrid = TetraMeshConversion.tetrameshTovtkUnstructuredGrid(renderable.mesh)
    }
    Caches.ScalarTetrahedralMeshFieldCache.getOrCreate(FastCachingTetrahedralMeshField(renderable.field), scalarVolumeMeshFieldToVtkUnstructuredGrid(renderable.field))
  }

  onInstantiated()

  rerender(geometryChanged = true)

}

abstract class TetrahedralActor3D[R <: TetrahedralRenderable](override val renderable: R) extends TetrahedralActor[R] {

  override protected def onInstantiated(): Unit = {
    mapper.SetInputData(unstructuredgrid)
  }

  override protected def onGeometryChanged(): Unit = {
    mapper.SetInputData(unstructuredgrid)
  }

}

abstract class TetrahedralActor2D[R <: TetrahedralRenderable](override val renderable: R, viewport: ViewportPanel2D) extends SlicingActor(viewport) with TetrahedralActor[R] with ActorLineWidth {
  override def lineWidth: LineWidthProperty = renderable.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(geometryChanged = false)

  override protected def onGeometryChanged(): Unit = {
    planeCutter.SetInputData(unstructuredgrid)
    planeCutter.Modified()
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(unstructuredgrid.GetBounds())
}

class TetrahedralMeshActor3D(node: TetrahedralMeshNode) extends TetrahedralActor3D(TetrahedralRenderable(node)) with TetrahedralMeshActor

class TetrahedralMeshActor2D(node: TetrahedralMeshNode, viewport: ViewportPanel2D) extends TetrahedralActor2D(TetrahedralRenderable(node), viewport) with TetrahedralMeshActor

class ScalarTetrahedralMeshFieldActor3D(node: ScalarTetrahedralMeshFieldNode) extends TetrahedralActor3D(TetrahedralRenderable(node)) with TetrahedralMeshScalarFieldActor

class ScalarTetrahedralMeshFieldActor2D(node: ScalarTetrahedralMeshFieldNode, viewport: ViewportPanel2D) extends TetrahedralActor2D(TetrahedralRenderable(node), viewport) with TetrahedralMeshScalarFieldActor