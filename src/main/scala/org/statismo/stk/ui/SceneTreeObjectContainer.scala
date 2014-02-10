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
  
  def removeAll = this.synchronized{
    println("before removeAll: "+_children.length)
    _children.foreach{ c =>
      println ("removing "+c)
      remove(c)
    }
    println("after removeAll: "+_children.length)
    
  }

  protected def remove(child: Child): Boolean = {
    println("trying to remove child, exists=" + children.exists(c=> c eq child))
    val before = _children.length
    val toRemove = _children.toIndexedSeq.zipWithIndex.filter { case (c,i) => c eq child }.map(_._2)
    println("toRemove: "+toRemove)
    toRemove.foreach{ idx =>
    	val child = _children(idx)
    	if (child.isInstanceOf[Removeable]) {
    	  deafTo(child.asInstanceOf[Removeable])
    	  child.asInstanceOf[Removeable].remove
    	}
    	_children.remove(idx)
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

