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

import scalismo.geometry.Point3D
import scalismo.io.MeshIO
import scalismo.mesh.ScalarMeshField
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.FileIoMetadata

import scala.util.Try

class ScalarMeshFieldsNode(override val parent: GroupNode) extends SceneNodeCollection[ScalarMeshFieldNode] {
  override val name: String = "Scalar Mesh Fields"

  def add(scalarMeshField: ScalarMeshField[Float], name: String): ScalarMeshFieldNode = {
    val node = new ScalarMeshFieldNode(this, scalarMeshField, name)
    add(node)
    node
  }
}

class ScalarMeshFieldNode(override val parent: ScalarMeshFieldsNode, override val source: ScalarMeshField[Float], initialName: String) extends Transformable[ScalarMeshField[Float]] with InverseTransformation with Saveable with Removeable with Renameable with HasOpacity with HasLineWidth with HasScalarRange {
  name = initialName

  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val scalarRange: ScalarRangeProperty = {
    val (min, max) = (source.values.min, source.values.max)
    new ScalarRangeProperty(ScalarRange(min, max, min, max))
  }

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.mesh.pointSet.findClosestPoint(point).id
    source.mesh.pointSet.point(id)
  }

  override def transform(untransformed: ScalarMeshField[Float], transformation: PointTransformation): ScalarMeshField[Float] = {
    untransformed.copy(mesh = untransformed.mesh.transform(transformation))
  }

  override def save(file: File): Try[Unit] = MeshIO.writeScalarMeshField[Float](transformedSource, file)

  override def saveMetadata: FileIoMetadata = FileIoMetadata.ScalarMeshField

}

