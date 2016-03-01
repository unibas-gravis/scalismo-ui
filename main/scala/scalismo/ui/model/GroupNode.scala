package scalismo.ui.model

import scalismo.ui.model.capabilities.Renameable

import scala.collection.immutable.Seq

class GroupsNode(override val parent: Scene) extends SceneNodeCollection[GroupNode] {
  override val name = "Groups"

  def add(name: String): GroupNode = {
    val node = new GroupNode(this, name)
    add(node)
    node
  }

  // the groups node is always collapsed in the view.
  override def isViewCollapsed: Boolean = true
}

class GroupNode(override val parent: GroupsNode, initialName: => String) extends SceneNode with Renameable {
  name = initialName

  val triangleMeshes = new TriangleMeshesNode(this)

  override val children: Seq[SceneNode] = List(triangleMeshes)
}

