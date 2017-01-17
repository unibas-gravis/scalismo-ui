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

import scalismo.ui.control.SceneControl
import scalismo.ui.event.Event
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.SceneNode.event.ChildrenChanged
import scalismo.ui.view.ScalismoFrame

object Scene {

  object event {

    case class SceneChanged(scene: Scene) extends Event

  }

}

class Scene extends SceneNode {
  override val name: String = "Scene"

  override lazy val scene: Scene = this

  override val parent: SceneNode = {
    /* A Scene is the only SceneNode without a parent, and should be handled accordingly.
     * If some algorithm carelessly tries to access the scene's parent, the NPE that will
     * get thrown is actually a feature ("you are doing something wrong!"), not a bug.
     */
    null
  }

  val groups = new GroupsNode(this)

  override val children: List[SceneNode] = List(groups)

  reactions += {
    case ChildrenChanged(node) => publishEvent(SceneChanged(this))
  }

}
