package scalismo.ui.api

/**
 * Created by marcel on 09.04.16.
 */

/**
 * This typeclass needs to be implemented if callbacks should be allowed for a view V
 */
protected[api] trait HandleCallback[V] {
  // calls function f if a node with type A has been added to the group g
  def registerOnAdd[R](g: Group, f: V => R)

  // calls function f if a node with type A has been removed from the group g
  def registerOnRemove[R](g: Group, f: V => R)

}

object HandleCallback {
  def apply[A](implicit a: HandleCallback[A]): HandleCallback[A] = a

}
