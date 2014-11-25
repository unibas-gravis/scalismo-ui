package org.statismo.stk.ui

import java.io.File

import org.statismo.stk.core.geometry.{ThreeD, Point, Point3D}
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.Reloadable.{FileReloader, ImmutableReloader, Reloader}

import scala.collection.immutable
import scala.util.{Failure, Success, Try}

object StaticPointCloud {
  def createFromPeer(peer: immutable.IndexedSeq[Point[ThreeD]], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticPointCloud = {
    new StaticPointCloud(peer, parent, name)
  }
}

class StaticPointCloud private[StaticPointCloud](override val peer: immutable.IndexedSeq[Point[ThreeD]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends PointCloud {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  override def addLandmarkAt(point: Point3D) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point)
  }

  parent.representations.add(this)
}