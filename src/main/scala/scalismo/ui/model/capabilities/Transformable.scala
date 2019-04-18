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

package scalismo.ui.model.capabilities

import scalismo.ui.event.Event
import scalismo.ui.model.{ GenericTransformationsNode, PointTransformation, ShapeModelTransformationsNode }

object Transformable {

  object event {

    case class GeometryChanged[T](source: Transformable[T]) extends Event

  }

}

trait Transformable[T] extends RenderableSceneNode with Grouped {
  def source: T // the untransformed T

  private def genericTransformationsNode: GenericTransformationsNode = group.genericTransformations

  private def shapeModelTransformationsNode: ShapeModelTransformationsNode = group.shapeModelTransformations

  private def combinedTransform = shapeModelTransformationsNode.combinedTransformation.map(smT => genericTransformationsNode.combinedTransformation compose smT) getOrElse {
    genericTransformationsNode.combinedTransformation
  }

  private var _transformedSource = transform(source, combinedTransform)

  def transformedSource: T = _transformedSource

  def transform(untransformed: T, transformation: PointTransformation): T

  def updateTransformedSource(): Unit = {
    _transformedSource = transform(source, combinedTransform)
    publishEvent(Transformable.event.GeometryChanged(this))
  }

  listenTo(genericTransformationsNode)
  listenTo(shapeModelTransformationsNode)

  reactions += {
    case GenericTransformationsNode.event.TransformationsChanged(_) => updateTransformedSource()
    case ShapeModelTransformationsNode.event.ShapeModelTransformationsChanged(_) => updateTransformedSource()
  }
}
