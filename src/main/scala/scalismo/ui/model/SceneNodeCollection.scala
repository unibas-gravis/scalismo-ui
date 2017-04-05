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

import scalismo.ui.model.capabilities.CollapsableView

import scala.collection.{ immutable, mutable }

object SceneNodeCollection {

  import scala.language.implicitConversions

  implicit def collectionAsChildNodeSeq[ChildNode <: SceneNode](collection: SceneNodeCollection[ChildNode]): immutable.Seq[ChildNode] = collection.children
}

trait SceneNodeCollection[ChildNode <: SceneNode] extends SceneNode with CollapsableView {
  private var _items = mutable.ListBuffer.empty[ChildNode]

  override final def children: List[ChildNode] = _items.toList

  protected def add(child: ChildNode): Unit = {
    require(child.parent == this)
    _items += child
    publishEvent(SceneNode.event.ChildAdded(this, child))
    publishEvent(SceneNode.event.ChildrenChanged(this))
  }

  protected def addToFront(child: ChildNode): Unit = {
    require(child.parent == this)
    _items.prepend(child)
    publishEvent(SceneNode.event.ChildAdded(this, child))
    publishEvent(SceneNode.event.ChildrenChanged(this))
  }

  def remove(child: ChildNode): Unit = {
    require(_items.contains(child))
    _items -= child
    publishEvent(SceneNode.event.ChildRemoved(this, child))
    publishEvent(SceneNode.event.ChildrenChanged(this))
  }

  // a collection is hidden in the tree view if it contains less than 2 items.
  override def isViewCollapsed: Boolean = _items.length < 2
}
