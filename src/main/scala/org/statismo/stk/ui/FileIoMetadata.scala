package org.statismo.stk.ui

trait FileIoMetadata {
  val fileExtensions: Seq[String]
  val description: String

  def longDescription: String = {
    description + fileExtensions.mkString(" (*.", ", *.", ")")
  }
}

object PngFileIoMetadata extends FileIoMetadata {
  override val description = "PNG Image"
  override val fileExtensions = Seq("png")
}