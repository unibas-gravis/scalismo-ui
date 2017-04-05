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

package scalismo.ui.api

import scalismo.geometry._3D
import scalismo.registration.RigidTransformation
import scalismo.ui.model.SceneNode.event.{ ChildAdded, ChildRemoved }
import scalismo.ui.model._
import scalismo.ui.view.ScalismoFrame

trait SimpleAPI {

  protected[api] def scene: Scene

  protected[api] val frame: ScalismoFrame

  def createGroup(groupName: String): Group = Group(scene.groups.add(groupName), frame)

  def show[A](a: A, name: String)(implicit showInScene: ShowInScene[A]): showInScene.View = showInScene.showInScene(a, name, defaultGroup, frame)

  def show[A](group: Group, a: A, name: String)(implicit showInScene: ShowInScene[A]): showInScene.View = showInScene.showInScene(a, name, group, frame)

  def addTransformation[T](g: Group, t: T, name: String)(implicit showInScene: ShowInScene[T]): showInScene.View = {
    showInScene.showInScene(t, name, g, frame)
  }

  def filter[V <: ObjectView: FindInScene](pred: V => Boolean): Seq[V] = {
    filterSceneNodes[V](scene, pred)
  }

  def filter[V <: ObjectView: FindInScene](group: Group, pred: V => Boolean): Seq[V] = {
    filterSceneNodes[V](group.peer, pred)
  }

  def find[V <: ObjectView: FindInScene](pred: V => Boolean): Option[V] =
    filter[V](pred).headOption

  def find[V <: ObjectView: FindInScene](group: Group, pred: V => Boolean): Option[V] =
    filter[V](group, pred).headOption

  def onNodeAdded[A <: ObjectView: HandleCallback, R](g: Group, f: A => R): Unit = {
    HandleCallback[A].registerOnAdd(g, f, frame)
  }

  def onNodeRemoved[A <: ObjectView: HandleCallback, R](g: Group, f: A => R): Unit = {
    HandleCallback[A].registerOnRemove(g, f, frame)
  }

  def onGroupAdded[R](f: Group => R): Unit = {
    scene.listenTo(scene.groups)

    scene.reactions += {
      case ChildAdded(collection, newNode: GroupNode) =>
        val gv = Group(newNode, frame)
        f(gv)
    }
  }

  def onGroupRemoved[R](f: Group => R): Unit = {
    scene.listenTo(scene.groups)

    scene.reactions += {
      case ChildRemoved(collection, newNode: GroupNode) =>
        val gv = Group(newNode, frame)
        f(gv)
    }
  }

  private def defaultGroup: Group = {
    val groupNode = scene.groups.find(g => g.name == "group")
      .getOrElse(scene.groups.add("group"))
    Group(groupNode, frame)
  }

  private def filterSceneNodes[V <: ObjectView: FindInScene](node: SceneNode, pred: V => Boolean): Seq[V] = {

    val resFromSubNodes = node.children.flatMap(child => filterSceneNodes(child, pred))

    val find = FindInScene[V]

    val head = find.createView(node, frame) match {
      case Some(v) => if (pred(v)) Seq[V](v) else Seq[V]()
      case None => Seq[V]()
    }

    head ++ resFromSubNodes
  }

}

