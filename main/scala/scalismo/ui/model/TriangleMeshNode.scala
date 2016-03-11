package scalismo.ui.model

import java.io.File

import scalismo.io.MeshIO
import scalismo.mesh.TriangleMesh
import scalismo.ui.model.capabilities.{ Saveable, Renameable, RenderableSceneNode }
import scalismo.ui.model.properties._
import scalismo.ui.util.FileIoMetadata

import scala.util.Try

class TriangleMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[TriangleMeshNode] {
  override val name: String = "Triangle Meshes"

  def add(mesh: TriangleMesh, name: String): TriangleMeshNode = {
    val node = new TriangleMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class TriangleMeshNode(override val parent: TriangleMeshesNode, val source: TriangleMesh, initialName: String) extends RenderableSceneNode with Saveable with Renameable with HasColor with HasOpacity with HasLineWidth {
  name = initialName

  override def save(file: File): Try[Unit] = MeshIO.writeMesh(source, file)

  override def saveMetadata: FileIoMetadata = FileIoMetadata.TriangleMesh

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
}

