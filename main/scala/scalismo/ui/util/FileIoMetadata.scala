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
    override val fileExtensions = List("vtk", "stl")
  }
}

