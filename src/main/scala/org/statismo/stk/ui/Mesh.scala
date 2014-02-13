package org.statismo.stk.ui

import java.io.File

import scala.swing.event.Event
import scala.util.Try

import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh

object Mesh {
  case class GeometryChanged(source: Mesh) extends Event
}

trait Mesh extends ThreeDRepresentation with Displayable with Colorable with Landmarkable with Saveable {
  def peer: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(peer, file)
  }

  override lazy val saveableMetadata = StaticMesh
}