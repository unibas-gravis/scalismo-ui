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
import scalismo.mesh.TetrahedralMesh3D
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{ FileIoMetadata, FileUtil }

import scala.util.{ Failure, Success, Try }

class TetrahedralMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[TetrahedralMeshNode] with Loadable {
  override val name: String = "Tetrahedral Meshes"

  override def loadMetadata: FileIoMetadata = FileIoMetadata.TetrahedralMeshRead

  override def load(file: File): Try[Unit] = {
    val r = MeshIO.readTetrahedralMesh(file)
    r match {
      case Failure(ex) => Failure(ex)
      case Success(mesh) =>
        add(mesh, FileUtil.basename(file))
        Success(())
    }
  }

  def add(mesh: TetrahedralMesh3D, name: String): TetrahedralMeshNode = {
    val node = new TetrahedralMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class TetrahedralMeshNode(override val parent: TetrahedralMeshesNode, override val source: TetrahedralMesh3D, initialName: String)
    extends Transformable[TetrahedralMesh3D] with InverseTransformation with Removeable with Renameable with HasLineWidth with HasOpacity with HasColor with Saveable with HasPickable {
  name = initialName

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.pointSet.findClosestPoint(point).id
    source.pointSet.point(id)
  }

  override def transform(untransformed: TetrahedralMesh3D, transformation: PointTransformation): TetrahedralMesh3D = {
    untransformed.transform(transformation)
  }

  override val opacity = new OpacityProperty()

  override val lineWidth = new LineWidthProperty()

  override val color = new ColorProperty()

  override val pickable = new PickableProperty()

  override def save(file: File): Try[Unit] = MeshIO.writeTetrahedralMesh(transformedSource, file)

  override def saveMetadata: FileIoMetadata = FileIoMetadata.TetrahedralMeshWrite

}