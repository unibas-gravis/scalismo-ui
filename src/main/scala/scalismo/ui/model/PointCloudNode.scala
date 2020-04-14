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

import scalismo.geometry.{_3D, Point, Point3D}
import scalismo.ui.model.capabilities.{InverseTransformation, Removeable, Renameable, Transformable}
import scalismo.ui.model.properties._

class PointCloudsNode(override val parent: GroupNode) extends SceneNodeCollection[PointCloudNode] {
  override val name: String = "Point Clouds"

  def add(pointCloud: PointCloud, name: String): PointCloudNode = {
    val node = new PointCloudNode(this, pointCloud, name)
    add(node)
    node
  }
}

class PointCloudNode(override val parent: PointCloudsNode, override val source: PointCloud, initialName: String)
    extends Transformable[PointCloud]
    with InverseTransformation
    with Removeable
    with Renameable
    with HasColor
    with HasOpacity
    with HasRadius
    with HasLineWidth {
  name = initialName

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
  override val radius = new RadiusProperty()
  override val lineWidth = new LineWidthProperty()

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

  override def transform(untransformed: PointCloud, transformation: PointTransformation): PointCloud = {
    untransformed.map(transformation)
  }

  override def inverseTransform(point: Point[_3D]): Point[_3D] = {
    val closest = transformedSource.map(_ - point).zipWithIndex.minBy(_._1.norm2)._2
    source(closest)
  }

}
