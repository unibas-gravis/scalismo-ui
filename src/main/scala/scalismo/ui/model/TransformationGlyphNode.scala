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

import scalismo.common.{ DiscreteField, UnstructuredPointsDomain }
import scalismo.geometry.{ Point3D, Vector3D, _3D, Vector }
import scalismo.ui.model.capabilities._

class TransformationGlyphNode(override val parent: VectorFieldsNode, val points: PointCloud, initialName: String)
    extends VectorFieldNode(parent, DiscreteField(UnstructuredPointsDomain(points), points.map(_ => Vector3D(0, 0, 0))), initialName) with Transformable[DiscreteField[_3D, Vector[_3D]]] with InverseTransformation {

  lazy val glyphPoints = points.toIndexedSeq

  override def transform(untransformed: DiscreteField[_3D, Vector[_3D]], transformation: PointTransformation): DiscreteField[_3D, Vector[_3D]] = {
    DiscreteField(untransformed.domain, glyphPoints.map(p => transformation(p) - p))
  }

  override def inverseTransform(point: Point3D): Point3D = point
}