package org.statismo.stk.ui

import org.statismo.stk.core.mesh.TriangleMesh
import java.io.File
import scala.util.Try
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.geometry.Point3D

object StaticMesh extends SceneTreeObjectFactory[StaticMesh] with FileIoMetadata {
  val description = "Static Mesh"
  val fileExtensions = Seq("h5","vtk")
  val metadata = this
  
  def apply(file: File)(implicit scene: Scene): Try[StaticMesh] = {
      apply(file, None, file.getName())
  }
  
  def apply(file: File, parent: Option[ThreeDRepresentations], name: String)(implicit scene: Scene): Try[StaticMesh] = {
    for {
      raw <-MeshIO.readMesh(file)
    } yield {
      val static = new StaticMesh(raw, parent, name)
      static
    }
  }
}

class StaticMesh(override val triangleMesh: TriangleMesh, initialParent: Option[ThreeDRepresentations] = None, initialName: String = "(no name)")(implicit override val scene: Scene) extends Mesh {
  name = initialName
  override lazy val parent: ThreeDRepresentations = initialParent.getOrElse {
    val p = new StaticThreeDObject(Some(scene.statics), initialName)
    p.representations
  }
  parent.add(this)
  
  def peer = triangleMesh
  
  def addLandmarkAt(point: Point3D) = {
    val landmarks = parent.parent.landmarks
    landmarks.addAt(point)
  }
}