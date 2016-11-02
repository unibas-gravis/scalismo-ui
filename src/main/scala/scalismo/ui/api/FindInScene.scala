package scalismo.ui.api

import scalismo.ui.model._
import scalismo.ui.view.ScalismoFrame

/**
 * This typeclass needs to be implemented for a type V (a view) if the user should be
 * able to search for a view
 */
protected[api] trait FindInScene[V] {
  def createView(s: SceneNode, frame: ScalismoFrame): Option[V]

}

object FindInScene {

  def apply[A](implicit a: FindInScene[A]): FindInScene[A] = a

}
