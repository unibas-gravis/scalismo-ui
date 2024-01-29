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

package scalismo.ui.model

import java.io.File

import scalismo.common.{DiscreteField, DomainWarp}
import scalismo.common.DiscreteField.ScalarMeshField
import scalismo.geometry.{_3D, Point, Point3D}
import scalismo.vtk.io.{MeshIO => MeshIOVtk}
import scalismo.mesh.TriangleMesh
import scalismo.transformations.Transformation
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{FileIoMetadata, FileUtil}

import scala.util.{Failure, Success, Try}

class ScalarMeshFieldsNode(override val parent: GroupNode)
    extends SceneNodeCollection[ScalarMeshFieldNode]
    with Loadable {

  override def loadMetadata: FileIoMetadata = FileIoMetadata.ScalarMeshField

  override val name: String = "Scalar Mesh Fields"

  def add(scalarMeshField: ScalarMeshField[Float], name: String): ScalarMeshFieldNode = {
    val node = new ScalarMeshFieldNode(this, scalarMeshField, name)
    add(node)
    node
  }

  override def load(file: File): Try[Unit] = {
    val r = MeshIOVtk.readScalarMeshFieldAsType[Float](file)
    r match {
      case Failure(ex) => Failure(ex)
      case Success(scalarMeshField) =>
        add(scalarMeshField, FileUtil.basename(file))
        Success(())
    }
  }

}

class ScalarMeshFieldNode(override val parent: ScalarMeshFieldsNode,
                          override val source: ScalarMeshField[Float],
                          initialName: String)(implicit canWarp: DomainWarp[_3D, TriangleMesh])
    extends Transformable[ScalarMeshField[Float]]
    with InverseTransformation
    with Saveable
    with Removeable
    with Renameable
    with HasOpacity
    with HasLineWidth
    with HasScalarRange {
  name = initialName

  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val scalarRange: ScalarRangeProperty = {
    new ScalarRangeProperty(ScalarRange(source.values.min, source.values.max))
  }

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

  override def inverseTransform(point: Point[_3D]): Point[_3D] = {
    val id = transformedSource.mesh.pointSet.findClosestPoint(point).id
    source.mesh.pointSet.point(id)
  }

  override def transform(untransformed: ScalarMeshField[Float],
                         transformation: PointTransformation): ScalarMeshField[Float] = {
    DiscreteField(canWarp.transform(untransformed.mesh, Transformation(transformation)), untransformed.data)
  }

  override def save(file: File): Try[Unit] = MeshIOVtk.writeScalarMeshField[Float](transformedSource, file)

  override def saveMetadata: FileIoMetadata = FileIoMetadata.ScalarMeshField

}
