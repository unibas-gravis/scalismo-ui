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

  def removeAll = {
    val copy = _children.map({ c => c })
    copy.foreach { c => remove(c) }

  }

  protected[ui] def remove(child: Child, silent: Boolean): Boolean = {
    val indexes = _children.toIndexedSeq.zipWithIndex.filter { case (c, i) => c eq child }.map(_._2)
    if (indexes.isEmpty) {
      false
    } else {
      if (!silent && child.isInstanceOf[Removeable]) {
        child.asInstanceOf[Removeable].remove
        true
      } else {
        val before = _children.length
        indexes.foreach { idx =>
          val child = _children(idx)
          if (!silent && child.isInstanceOf[Removeable]) {
            deafTo(child.asInstanceOf[Removeable])
          }
          _children.remove(idx)
        }
        val after = _children.length
        before != after
      }
    }
  }

  def remove(child: Child): Boolean = {
    remove(child, false)
  }

  reactions += {
    case Removeable.Removed(c) => {
      val child = c.asInstanceOf[Child]
      remove(child, true)
    }
  }

}

trait SceneTreeObjectContainer[Child <: SceneTreeObject] extends SceneTreeObject with MutableObjectContainer[Child] {
  override def children = super.children

  override def addAll(newChildren: Seq[Child]): Unit = {
    super.addAll(newChildren)
    publish(SceneTreeObject.ChildrenChanged(this))
  }

  override def remove(child: Child, silent: Boolean): Boolean = {
    val changed = super.remove(child, silent)
    if (changed) {
      publish(SceneTreeObject.ChildrenChanged(this))
    }
    changed
  }
}

