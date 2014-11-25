package org.statismo.stk.ui

import java.io.File

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.Reloadable.{FileReloader, ImmutableReloader, Reloader}

import scala.collection.immutable
import scala.util.{Failure, Success, Try}

object StaticMesh extends SceneTreeObjectFactory[StaticMesh] with FileIoMetadata {
  override val description = "Static Mesh"
  override val fileExtensions = immutable.Seq("vtk", "stl")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticMesh] = {
    createFromFile(file, None, file.getName)
  }

  def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticMesh] = {
    Try {
      new FileReloader[TriangleMesh](file) {
        override def doLoad() = MeshIO.readMesh(file)
      }
    }.map(reloader => new StaticMesh(reloader, parent, Some(name)))
  }

  def createFromPeer(peer: TriangleMesh, parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticMesh = {
    new StaticMesh(new ImmutableReloader[TriangleMesh](peer), parent, name)
  }
}

class StaticMesh private[StaticMesh](peerLoader: Reloader[TriangleMesh], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Mesh with Reloadable {

  override def peer = _peer
  private var _peer = peerLoader.load().get

  name_=(name.getOrElse(Nameable.NoName))

  override def reload() = {
    peerLoader.load() match {
      case ok@Success(newPeer) =>
        if (newPeer != peer) {
          _peer = newPeer
          publishEdt(Mesh.Reloaded(this))
        }
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  override def isCurrentlyReloadable = peerLoader.isReloadable

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  override def addLandmarkAt(point: Point3D, nameOpt: Option[String]) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point, nameOpt)
  }

  parent.representations.add(this)
}