package org.statismo.stk.ui

import java.io.File

import scala.util.Try

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh

object StaticMesh extends SceneTreeObjectFactory[StaticMesh] with FileIoMetadata {
  val description = "Static Mesh"
  val fileExtensions = Seq("h5", "vtk")
  val ioMetadata = this

  def apply(file: File)(implicit scene: Scene): Try[StaticMesh] = {
    apply(file, None, file.getName())
  }

  def apply(file: File, parent: Option[ThreeDObject], name: String)(implicit scene: Scene): Try[StaticMesh] = {
    for {
      raw <- MeshIO.readMesh(file)
    } yield {
      new StaticMesh(raw, parent, Some(name))
    }
  }
}

class StaticMesh(override val peer: TriangleMesh, initialParent: Option[ThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Mesh {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: ThreeDObject = initialParent.getOrElse (new StaticThreeDObject(Some(scene.staticObjects), name))

  def addLandmarkAt(point: Point3D) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point)
  }

  parent.representations.add(this)
}