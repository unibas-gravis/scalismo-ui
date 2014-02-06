package org.statismo.stk.ui

import scala.collection.mutable.ArrayBuffer

trait SceneTreeObjectContainer[Child <: SceneTreeObject] extends SceneTreeObject {
  protected var _children: ArrayBuffer[Child] = new ArrayBuffer
  override def children: Seq[Child] = _children

  def add(newChild: Child): Unit = {
    addAll(Seq(newChild))
  }
  
  def addAll(newChildren: Seq[Child]): Unit = {
    _children ++= newChildren
    publish(SceneTreeObject.ChildrenChanged(this))
  }
}

