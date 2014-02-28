package org.statismo.stk.ui

import scala.swing.Reactor

trait MutableObjectContainer[Child <: AnyRef] extends Reactor {
  protected var _children: IndexedSeq[Child] = Nil.toIndexedSeq
  def children: Seq[Child] = _children

  def add(child: Child): Unit = {
    if (child.isInstanceOf[Removeable]) {
      listenTo(child.asInstanceOf[Removeable])
    }
    _children = Seq(_children, Seq(child)).flatten.toIndexedSeq
  }

  def apply(index: Int) = children(index)

  def removeAll = {
    val copy = _children.map({ c => c })
    copy.foreach { c => remove(c) }
  }

  protected[ui] def remove(child: Child, silent: Boolean): Boolean = {
    if (!silent && child.isInstanceOf[Removeable]) {
      // this will publish a message which is handled in the reactions
      child.asInstanceOf[Removeable].remove
      true
    } else {
      val before = _children.length
      val toRemove = _children filter (_ eq child)
      toRemove foreach { child =>
        if (!silent && child.isInstanceOf[Removeable]) {
          deafTo(child.asInstanceOf[Removeable])
        }
      }
      _children = _children.diff(toRemove)
      val after = _children.length
      before != after
    }
  }

  def remove(child: Child): Boolean = {
    remove(child, silent = false)
  }

  reactions += {
    case Removeable.Removed(c) =>
      val child = c.asInstanceOf[Child]
      remove(child, silent = true)
  }

}

trait SceneTreeObjectContainer[Child <: SceneTreeObject] extends MutableObjectContainer[Child] {
//  override def children = super.children // required to prevent type conflict

  def publisher: SceneTreeObject
  
  override def add(child: Child): Unit = {
    super.add(child)
    publisher.publish(SceneTreeObject.ChildrenChanged(publisher))
  }

  override def remove(child: Child, silent: Boolean): Boolean = {
    val changed = super.remove(child, silent)
    if (changed) {
      publisher.publish(SceneTreeObject.ChildrenChanged(publisher))
    }
    changed
  }
}

trait StandaloneSceneTreeObjectContainer[Child <: SceneTreeObject] extends SceneTreeObject with SceneTreeObjectContainer[Child] {
  override def children = super.children // required to prevent type conflict
  override lazy val publisher = this
}

