package scalismo.ui

import scala.collection.immutable

trait FileIoMetadata {
  def fileExtensions: immutable.Seq[String]
  def description: String

  def longDescription: String = {
    description + fileExtensions.mkString(" (*.", ", *.", ")")
  }
}

object PngFileIoMetadata extends FileIoMetadata {
  override val description = "PNG Image"
  override val fileExtensions = immutable.Seq("png")
}