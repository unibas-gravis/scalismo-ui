package scalismo.ui.api

import scalismo.ui.view.ScalismoFrame

/**
 * This typeclass needs to be implemented if callbacks should be allowed for a view V
 */
protected[api] trait HandleCallback[V] {
  // calls function f if a node with type A has been added to the group g
  def registerOnAdd[R](g: Group, f: V => R, frame: ScalismoFrame)

  // calls function f if a node with type A has been removed from the group g
  def registerOnRemove[R](g: Group, f: V => R, frame: ScalismoFrame)

}

object HandleCallback {
  def apply[A](implicit a: HandleCallback[A]): HandleCallback[A] = a

}
