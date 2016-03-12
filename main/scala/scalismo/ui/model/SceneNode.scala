package scalismo.ui.model

import scalismo.ui.control.NodeVisibility.SceneNodeWithVisibility
import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.capabilities.RenderableSceneNode
import scalismo.ui.view.ScalismoFrame

object SceneNode {

  import scala.language.implicitConversions

  object event {

    case class ChildrenChanged(node: SceneNode) extends Event

  }

  /**
   * Returns a "pimped" (as in "pimp my library") version of this node, which allows to set its visibility in a particular view context (i.e., frame).
   *
   * Strictly speaking, this goes against the convention of a model being completely agnostic of its view. But it's incredibly useful, and since it's an implicit,
   * it can only ever be invoked from a view context anyway.
   *
   * This has to be defined here, because otherwise all view code would have to import an additional implicit method or class.
   *
   */
  implicit def sceneNodeAsSceneNodeWithVisibility(node: SceneNode)(implicit frame: ScalismoFrame): SceneNodeWithVisibility = new SceneNodeWithVisibility(node)(frame)
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

