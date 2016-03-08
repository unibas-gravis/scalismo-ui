package scalismo.ui.model

import scalismo.ui.event.{ Event, ScalismoPublisher }

import scala.collection.immutable

object SceneNode {

  object event {

    case class ChildrenChanged(node: SceneNode) extends Event

  }

}

trait SceneNode extends ScalismoPublisher {
  def name: String

  /**
   * Returns this node's parent [[SceneNode]], or null if this node is itself a [[Scene]].
   *
   * @return this node's parent [[SceneNode]], or null if this node is itself a [[Scene]]
   */
  def parent: SceneNode

  lazy val scene: Scene = parent.scene

  def children: immutable.Seq[SceneNode] = Nil

  override def toString: String = name

  // the scene listens to events on all nodes
  scene.listenTo(this)
}

