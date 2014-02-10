package org.statismo.stk.ui

import scala.swing.event.Event
import org.statismo.stk.core.mesh.TriangleMesh
import java.io.File
import scala.util.Try
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO

object ThreeDImage {
//  case class GeometryChanged(source: Mesh) extends Event
}

trait ThreeDImage extends ThreeDRepresentation with Colorable with Landmarkable with Saveable {
  def peer: DiscreteScalarImage3D[Short]

  override def saveToFile(file: File): Try[Unit] = {
    ImageIO.writeImage(peer, file)
  }
  
  override lazy val saveableMetadata = StaticImage
}