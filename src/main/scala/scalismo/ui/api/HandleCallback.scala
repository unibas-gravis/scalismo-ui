package scalismo.ui.api

import scalismo.ui.model.SceneNode.event.{ ChildRemoved, ChildAdded, ChildrenChanged }
import scalismo.ui.model.{ SceneNode, GroupNode, TriangleMeshNode, TriangleMeshesNode }

import scala.swing.event.Event

/**
 * Created by marcel on 09.04.16.
 */

trait HandleCallback[A] {
  def registerOnAdd[R](g: Group, f: A => R)
  def registerOnRemove[R](g: Group, f: A => R)

}

object HandleCallback {
  def apply[A](implicit a: HandleCallback[A]): HandleCallback[A] = a

}
