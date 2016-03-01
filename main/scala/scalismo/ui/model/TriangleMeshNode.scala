package scalismo.ui.model

import scalismo.mesh.TriangleMesh
import scalismo.ui.model.capabilities.Renameable

class TriangleMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[TriangleMeshNode] {
  override val name: String = "Triangle Meshes"

  def add(mesh: TriangleMesh, name: String): TriangleMeshNode = {
    val node = new TriangleMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class TriangleMeshNode(override val parent: TriangleMeshesNode, val mesh: TriangleMesh, initialName: => String) extends SceneNode with Renameable {
  name = initialName
}

