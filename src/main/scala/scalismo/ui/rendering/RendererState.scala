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

package scalismo.ui.rendering

import java.awt.Point

import scalismo.geometry.Point3D
import scalismo.ui.model.SceneNode
import scalismo.ui.rendering.RendererState.PointAndNode

object RendererState {

  case class PointAndNode(pointOption: Option[Point3D], nodeOption: Option[SceneNode])

}

trait RendererState {
  def pointAndNodeAtPosition(point: Point): PointAndNode

  def isHighlightable(node: SceneNode): Boolean

  def setHighlighted(node: SceneNode, onOff: Boolean): Unit
}
