package scalismo.ui.model

import scalismo.ui.model.capabilities.CollapsableView

import scala.collection.immutable.Seq
import scala.collection.{ mutable, immutable }

object SceneNodeCollection {
  import scala.language.implicitConversions
  implicit def collectionAsChildNodeSeq[ChildNode <: SceneNode](collection: SceneNodeCollection[ChildNode]): immutable.Seq[ChildNode] = collection.children
}

trait SceneNodeCollection[ChildNode <: SceneNode] extends SceneNode with CollapsableView {
  private var _items = mutable.ListBuffer.empty[ChildNode]

  override final def children: Seq[ChildNode] = _items.toList

  def add(child: ChildNode): Unit = {
    require(child.parent == this)
    _items += child
  }

  def remove(child: ChildNode): Unit = {
    _items -= child
  }

  // a collection is hidden in the tree view if it contains less than 2 items.
  override def isViewCollapsed: Boolean = _items.length < 2
}
