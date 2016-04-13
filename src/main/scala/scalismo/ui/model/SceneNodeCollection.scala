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
