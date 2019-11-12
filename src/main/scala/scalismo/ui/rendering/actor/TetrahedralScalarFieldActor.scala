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
package scalismo.ui.rendering.actor

import scalismo.common.{Scalar, UnstructuredPointsDomain}
import scalismo.geometry._3D
import scalismo.mesh._
import scalismo.tetramesh.ScalarVolumeMeshField
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.model.{BoundingBox, ScalarFieldNode, ScalarTetrahedralMeshFieldNode}
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ViewportPanel, ViewportPanel2D, ViewportPanel3D}
import scalismo.utils.{MeshConversion, VtkHelpers}
import vtk._
import scala.reflect.runtime.universe.TypeTag

import scala.reflect.ClassTag

object TetrahedralScalarFieldActor extends SimpleActorsFactory[ScalarTetrahedralMeshFieldNode] {
  override def actorsFor(renderable: ScalarTetrahedralMeshFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new ScalarTetrahedralMeshFieldActor3D(renderable))
      case _2d: ViewportPanel2D => None //Some(new ScalarTetrahedralMeshFieldActor2D(renderable, _2d))
    }
  }
}

trait TetrahedralScalarFieldActor extends UnstructuredGridActor with ActorOpacity with ActorScalarGridRange with ActorSceneNode {
  override def sceneNode: ScalarTetrahedralMeshFieldNode

  override def opacity: OpacityProperty = sceneNode.opacity

  def scalarRange: ScalarRangeProperty = sceneNode.scalarRange

  protected def onInstantiated(): Unit

  def transformedPoints: vtkPoints = new vtkPoints {
    sceneNode.transformedSource.domain.points.foreach { point =>
      InsertNextPoint(point(0), point(1), point(2))
    }
  }

  protected var unstructuredgrid: vtkUnstructuredGrid = meshToUnstructuredGrid(None)


  def scalarVolumeMeshFieldToVtkPolyData[S: Scalar : ClassTag : TypeTag](tetraMeshData: ScalarVolumeMeshField[S]): vtkUnstructuredGrid = { //note copied from Conversions.scalarArrayToVtkDataArray
    val scalarData = VtkHelpers.scalarArrayToVtkDataArray(tetraMeshData.data, 1) // TODO make this more general
    unstructuredgrid.GetPointData().SetScalars(scalarData)
    unstructuredgrid
  }

  protected def meshToUnstructuredGrid(template: Option[vtkUnstructuredGrid]): vtkUnstructuredGrid = { //todo
    scalarVolumeMeshFieldToVtkPolyData(sceneNode.source)
  }

  def onGeometryChanged(): Unit

  def rerender(geometryChanged: Boolean): Unit = {
    if (geometryChanged) {
      unstructuredgrid = meshToUnstructuredGrid(Some(unstructuredgrid))
      onGeometryChanged()
    }

    actorChanged(geometryChanged)
  }

  listenTo(sceneNode)//, sceneNode.source)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(true)
  }

  onInstantiated()

  rerender(true)

}

class ScalarTetrahedralFieldActor2D(override val sceneNode: ScalarTetrahedralMeshFieldNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with TetrahedralScalarFieldActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = sceneNode.lineWidth

  override protected def onInstantiated(): Unit = {
    mapper.SetInputData(unstructuredgrid)
  }

  override protected def onSlicingPositionChanged(): Unit = rerender(geometryChanged = false)

  override protected def onGeometryChanged(): Unit = {
    planeCutter.SetInputData(unstructuredgrid)
    planeCutter.Modified()
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(unstructuredgrid.GetBounds())
}

class ScalarTetrahedralFieldActor3D(override val sceneNode: ScalarTetrahedralMeshFieldNode) extends TetrahedralScalarFieldActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputData(unstructuredgrid)

  }

  override protected def onGeometryChanged(): Unit = {
  }


}*/ 