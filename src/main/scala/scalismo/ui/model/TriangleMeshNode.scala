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

import scalismo.geometry.{ Point3D, _3D }
import scalismo.io.MeshIO
import scalismo.mesh.TriangleMesh
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{ FileIoMetadata, FileUtil }

import scala.util.{ Failure, Success, Try }

class TriangleMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[TriangleMeshNode] with Loadable {
  override val name: String = "Triangle Meshes"

  override def loadMetadata: FileIoMetadata = FileIoMetadata.TriangleMesh

  override def load(file: File): Try[Unit] = {
    val r = MeshIO.readMesh(file)
    r match {
      case Failure(ex) => Failure(ex)
      case Success(mesh) =>
        add(mesh, FileUtil.basename(file))
        Success(())
    }
  }

  def add(mesh: TriangleMesh[_3D], name: String): TriangleMeshNode = {
    val node = new TriangleMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class TriangleMeshNode(override val parent: TriangleMeshesNode, override val source: TriangleMesh[_3D], initialName: String) extends Transformable[TriangleMesh[_3D]] with InverseTransformation with Saveable with Renameable with Removeable with HasColor with HasOpacity with HasLineWidth with HasPickable {
  name = initialName

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val pickable = new PickableProperty()

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.pointSet.findClosestPoint(point).id
    source.pointSet.point(id)
  }

  override def group: GroupNode = parent.parent

  override def transform(untransformed: TriangleMesh[_3D], transformation: PointTransformation): TriangleMesh[_3D] = {
    untransformed.transform(transformation)
  }

  override def save(file: File): Try[Unit] = MeshIO.writeMesh(transformedSource, file)

  override def saveMetadata: FileIoMetadata = FileIoMetadata.TriangleMesh

  override def remove(): Unit = parent.remove(this)

}

