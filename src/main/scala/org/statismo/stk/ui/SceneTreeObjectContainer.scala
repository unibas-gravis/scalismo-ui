package org.statismo.stk.ui

import scala.collection.mutable.ArrayBuffer
import scala.swing.Publisher
import scala.swing.Reactor

trait MutableObjectContainer[Child <: AnyRef] extends Reactor {
  protected var _children: ArrayBuffer[Child] = new ArrayBuffer
  def children: Seq[Child] = _children

  def add(newChild: Child): Unit = {
    addAll(Seq(newChild))
  }

  def addAll(newChildren: Seq[Child]): Unit = {
    newChildren foreach { c =>
      if (c.isInstanceOf[Removeable]) {
        listenTo(c.asInstanceOf[Removeable])
      }
    }
    _children ++= newChildren
  }

  def apply(index: Int) = children(index)

  def remove(child: Child): Boolean = {
    val before = _children.length
    if (_children.exists({ x => x eq child })) {
      val index = _children.indexOf(child)
      if (child.isInstanceOf[Removeable]) {
        deafTo(child.asInstanceOf[Removeable])
        child.asInstanceOf[Removeable].remove()
      }
      _children.remove(index)
    }
    val after = _children.length
    before != after
  }

  reactions += {
    case Removeable.Removed(c) => {
      val child = c.asInstanceOf[Child]
      remove(child)
    }
  }

}

trait SceneTreeObjectContainer[Child <: SceneTreeObject] extends SceneTreeObject with MutableObjectContainer[Child] {
  override def children = super.children

  override def addAll(newChildren: Seq[Child]): Unit = {
    super.addAll(newChildren)
    publish(SceneTreeObject.ChildrenChanged(this))
  }

  override def remove(child: Child): Boolean = {
    val changed = super.remove(child)
    if (changed) {
      publish(SceneTreeObject.ChildrenChanged(this))
    }
    changed
  }
}

