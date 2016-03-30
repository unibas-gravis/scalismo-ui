package scalismo.ui.model

import java.io.File

import scalismo.geometry.Point3D
import scalismo.io.MeshIO
import scalismo.mesh.TriangleMesh
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{ FileIoMetadata, FileUtil }

import scala.util.{ Failure, Success, Try }

class TriangleMeshesNode(override val parent: GroupNode) extends SceneNodeCollection[TriangleMeshNode] with Loadable {
  override val name: String = "Triangle Meshes"

  override def loadMetadata: FileIoMetadata = FileIoMetadata.TriangleMesh

  override def load(file: File): Try[Unit] = {
    val r = MeshIO.readMesh(file)
    r match {
      case Failure(ex) => Failure(ex)
      case Success(mesh) =>
        add(mesh, FileUtil.basename(file))
        Success(())
    }
  }

  def add(mesh: TriangleMesh, name: String): TriangleMeshNode = {
    val node = new TriangleMeshNode(this, mesh, name)
    add(node)
    node
  }
}

class TriangleMeshNode(override val parent: TriangleMeshesNode, override val source: TriangleMesh, initialName: String) extends Transformable[TriangleMesh] with InverseTransformation with Saveable with Renameable with Removeable with HasColor with HasOpacity with HasLineWidth with HasPickable {
  name = initialName

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val pickable = new PickableProperty()

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.findClosestPoint(point).id
    source.point(id)
  }

  override def group: GroupNode = parent.parent

  override def transform(untransformed: TriangleMesh, transformation: PointTransformation): TriangleMesh = {
    untransformed.transform(transformation)
  }

  override def save(file: File): Try[Unit] = MeshIO.writeMesh(transformedSource, file)

  override def saveMetadata: FileIoMetadata = FileIoMetadata.TriangleMesh

  override def remove(): Unit = parent.remove(this)

}

