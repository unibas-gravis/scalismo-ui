package scalismo.ui.model

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.capabilities.RenderableSceneNode

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
   */
  def parent: SceneNode

  lazy val scene: Scene = parent.scene

  def children: List[SceneNode] = Nil

  override def toString: String = name

  def renderables: List[RenderableSceneNode] = {
    children.flatMap(_.renderables)
  }

  // the scene listens to events on all nodes
  scene.listenTo(this)
}

