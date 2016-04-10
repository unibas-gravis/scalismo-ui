package scalismo.ui.api

import scalismo.geometry.{Point, _3D}
import scalismo.registration.RigidTransformation
import scalismo.ui.model._

/**
  * Created by luetma00 on 08.04.16.
  */

trait FindInScene[V] {
  def createView(s : SceneNode) : Option[V]

}


object FindInScene {

  def apply[A](implicit a : FindInScene[A]) : FindInScene[A] = a



}
