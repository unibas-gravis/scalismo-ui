package scalismo.ui.model

import scalismo.ui.event.Event
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.SceneNode.event.ChildrenChanged

import scala.collection.immutable.Seq

object Scene {
  object event {
    case class SceneChanged(scene: Scene) extends Event
  }
}

class Scene extends SceneNode {
  override val name: String = "Scene"

  override lazy val scene: Scene = this

  override val parent: SceneNode = {
    // should actually be null, but we play it safe.
    this
  }

  val groups = new GroupsNode(this)

  override val children: Seq[SceneNode] = List(groups)

  reactions += {
    case ChildrenChanged(node) => publishEdt(SceneChanged(this))
  }
}
