package scalismo.ui.util

/**
 * Metadata for File I/O.
 *
 * This trait is used to provide the correct
 * information to UI elements like file choosers.
 */
trait FileIoMetadata {
  /** The supported file extensions (no leading dots) */
  def fileExtensions: List[String]

  /** Human-readable description */
  def description: String

  def longDescription: String = {
    description + fileExtensions.mkString(" (*.", ", *.", ")")
  }
}

object FileIoMetadata {

  val Png = new FileIoMetadata {
    override val description = "PNG Image"
    override val fileExtensions = List("png")
  }

  val TriangleMesh = new FileIoMetadata {
    override val description = "Triangle Mesh"
    override val fileExtensions = List("stl", "vtk")
  }


  val ScalarMeshField = new FileIoMetadata {
    override val description = "Scalar Mesh Field"
    override val fileExtensions = List("vtk")
  }

  val Image = new FileIoMetadata {
    override val description = "3D Image"
    override val fileExtensions = List("nii", "vtk")
  }

  val Landmarks = new FileIoMetadata {
    override val description = "Landmarks"
    override val fileExtensions = List("json", "csv")
  }

  val StatisticalShapeModel = new FileIoMetadata {
    override val description = "Statistical Shape Model"
    override val fileExtensions = List("h5")
  }

}

