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

import scalismo.common.{DiscreteDomain, DiscreteField, UnstructuredPointsDomain}
import scalismo.geometry._
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._

import scala.collection.immutable

class VectorFieldsNode(override val parent: GroupNode) extends SceneNodeCollection[VectorFieldNode] {
  override val name: String = "Scalar fields"

  def add(vectorField: DiscreteField[_3D, UnstructuredPointsDomain, EuclideanVector[_3D]], name: String): VectorFieldNode = {
    val node = new VectorFieldNode(this, vectorField, name)
    add(node)
    node
  }

  def addTransformationGlyph(pointCloud: PointCloud, name: String): TransformationGlyphNode = {
    val node = new TransformationGlyphNode(this, pointCloud, name)
    add(node)
    node
  }
}

class VectorFieldNode(override val parent: VectorFieldsNode,
                      val source: DiscreteField[_3D, UnstructuredPointsDomain, EuclideanVector[_3D]],
                      initialName: String)
    extends RenderableSceneNode
    with Removeable
    with Renameable
    with Grouped
    with HasOpacity
    with HasLineWidth
    with HasScalarRange {

  name = initialName

  // we store the vectors as a sequence, as values are defined by iterators, which we cannot
  // traverse twice
  private lazy val vectors: immutable.IndexedSeq[EuclideanVector[_3D]] = source.values.toIndexedSeq

  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override lazy val scalarRange: ScalarRangeProperty = {
    val (min, max) = {
      val norms = vectors.map(_.norm)
      (norms.min.toFloat, norms.max.toFloat)
    }
    new ScalarRangeProperty(ScalarRange(min, max))
  }

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

}
