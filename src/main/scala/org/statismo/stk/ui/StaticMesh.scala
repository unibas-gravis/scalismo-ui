package org.statismo.stk.ui

import java.io.File

import scala.util.Try

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh
import scala.collection.immutable

object StaticMesh extends SceneTreeObjectFactory[StaticMesh] with FileIoMetadata {
  override val description = "Static Mesh"
  override val fileExtensions = immutable.Seq("vtk")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticMesh] = {
    apply(file, None, file.getName)
  }

  def apply(file: File, parent: Option[ThreeDObject], name: String)(implicit scene: Scene): Try[StaticMesh] = {
    for {
      raw <- MeshIO.readMesh(file)
    } yield {
      new StaticMesh(raw, parent, Some(name))
    }
  }
}

class StaticMesh protected[ui] (override val peer: TriangleMesh, initialParent: Option[ThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Mesh {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: ThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  def addLandmarkAt(point: Point3D) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point)
  }

  parent.representations.add(this)
}