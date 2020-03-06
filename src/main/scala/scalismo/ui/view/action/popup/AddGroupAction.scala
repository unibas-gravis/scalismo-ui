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

package scalismo.ui.view.action.popup

import scalismo.ui.model.{Scene, SceneNode}
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.AskForInputAction

object AddGroupAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[Scene](context).map(n => new AddGroupAction(n)).toList
  }
}

class AddGroupAction(node: Scene)(implicit val frame: ScalismoFrame)
    extends PopupAction("Add Group ...", BundledIcon.Group) {
  def callback(newName: Option[String]): Unit = {
    newName.foreach(n => node.groups.add(n))
  }

  override def apply(): Unit = {
    new AskForInputAction[String](s"Enter a name for the new group:", "", callback, "Add Group").apply()
  }
}
