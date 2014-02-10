package org.statismo.stk.ui

import scala.swing.event.Event
import org.statismo.stk.core.mesh.TriangleMesh
import java.io.File
import scala.util.Try
import org.statismo.stk.core.io.MeshIO

object Mesh {
  case class GeometryChanged(source: Mesh) extends Event
}

trait Mesh extends ThreeDRepresentation with Colorable with Landmarkable with Saveable {
  def triangleMesh: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(triangleMesh, file)
  }
  
  override lazy val saveableMetadata = StaticMesh
}