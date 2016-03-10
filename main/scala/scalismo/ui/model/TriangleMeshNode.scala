package scalismo.ui.model

import scalismo.mesh.TriangleMesh
import scalismo.ui.model.capabilities.{ Renameable, RenderableSceneNode }
import scalismo.ui.model.properties._

class TriangleMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[TriangleMeshNode] {
  override val name: String = "Triangle Meshes"

  def add(mesh: TriangleMesh, name: String): TriangleMeshNode = {
    val node = new TriangleMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class TriangleMeshNode(override val parent: TriangleMeshesNode, val source: TriangleMesh, initialName: String) extends RenderableSceneNode with Renameable with HasColor with HasOpacity with HasLineWidth {
  name = initialName

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
}

