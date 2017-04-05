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
import scalismo.mesh.{ LineMesh, TriangleMesh }
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{ FileIoMetadata, FileUtil }

import scala.util.{ Failure, Success, Try }

class LineMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[LineMeshNode] {
  override val name: String = "Line Meshes"

  def add(mesh: LineMesh[_3D], name: String): LineMeshNode = {
    val node = new LineMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class LineMeshNode(override val parent: LineMeshesNode, override val source: LineMesh[_3D], initialName: String) extends Transformable[LineMesh[_3D]] with InverseTransformation with Renameable with Removeable with HasColor with HasOpacity with HasLineWidth {
  name = initialName

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.pointSet.findClosestPoint(point).id
    source.pointSet.point(id)
  }

  override def group: GroupNode = parent.parent

  override def transform(untransformed: LineMesh[_3D], transformation: PointTransformation): LineMesh[_3D] = {
    untransformed.transform(transformation)
  }

  override def remove(): Unit = parent.remove(this)

}

