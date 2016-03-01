package scalismo.ui.model

import scala.collection.immutable.Seq

class Scene extends SceneNode {
  override val name: String = "Scene"

  override lazy val scene: Scene = this

  override val parent: SceneNode = {
    // should actually be null, but we play it safe.
    this
  }

  val groups = new GroupsNode(this)

  override val children: Seq[SceneNode] = List(groups)
}
