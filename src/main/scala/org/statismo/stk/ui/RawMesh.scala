package org.statismo.stk.ui

import java.io.File
import scala.util.Try
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh

object RawMesh extends Loadable[RawMesh] {
  val description = "Raw Mesh"
  val fileExtensions = Seq[String]("h5","vtk")
  
  def apply(file: File): Try[RawMesh] = {
    for {
      raw <- MeshIO.readMesh(file)
    } yield new RawMesh(raw, file.getName())
  }
}

class RawMesh(val mesh: TriangleMesh, val displayName: String) extends Surface {
  
}

