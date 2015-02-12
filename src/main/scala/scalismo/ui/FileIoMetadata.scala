package scalismo.ui

import scala.collection.immutable

trait FileIoMetadata {
  val fileExtensions: immutable.Seq[String]
  val description: String

  def longDescription: String = {
    description + fileExtensions.mkString(" (*.", ", *.", ")")
  }
}

object PngFileIoMetadata extends FileIoMetadata {
  override val description = "PNG Image"
  override val fileExtensions = immutable.Seq("png")
}