package org.statismo.stk.ui

trait SceneTreeObjectContainer[Child <: SceneTreeObject] extends SceneTreeObject {
  private var _children: List[Child] = Nil
  override def children: Seq[Child] = _children

  def add(newChildren: Seq[Child]) = {
    _children = _children ::: List(newChildren).flatten
    publish(SceneTreeObject.ChildrenChanged(this))
  }
}

