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

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.capabilities.RenderableSceneNode

object SceneNode {

  object event {

    case class ChildrenChanged(nodeCollection: SceneNode) extends Event

    case class ChildAdded(nodeCollection: SceneNode, addedNode: SceneNode) extends Event

    case class ChildRemoved(nodeCollection: SceneNode, removedNode: SceneNode) extends Event

  }

}

trait SceneNode extends ScalismoPublisher {
  def name: String

  /**
   * Returns this node's parent [[SceneNode]], or null if this node is itself a [[Scene]].
   *
   */
  def parent: SceneNode

  lazy val scene: Scene = parent.scene

  def children: List[SceneNode] = Nil

  override def toString: String = name

  def renderables: List[RenderableSceneNode] = {
    children.flatMap(_.renderables)
  }

  // the scene listens to events on all nodes
  scene.listenTo(this)
}

