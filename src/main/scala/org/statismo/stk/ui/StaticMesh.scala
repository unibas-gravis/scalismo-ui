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
    createFromFile(file, None, file.getName)
  }

  def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticMesh] = {
    for {
      raw <- MeshIO.readMesh(file)
    } yield {
      new StaticMesh(raw, parent, Some(name))
    }
  }

  def createFromPeer(peer: TriangleMesh, parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticMesh = {
    new StaticMesh(peer, parent, name)
  }
}

class StaticMesh protected[ui] (override val peer: TriangleMesh, initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Mesh {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  def addLandmarkAt(point: Point3D) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point)
  }

  parent.representations.add(this)
}