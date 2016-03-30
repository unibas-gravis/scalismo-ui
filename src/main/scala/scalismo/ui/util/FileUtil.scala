package scalismo.ui.util

import java.io.File

object FileUtil {
  /**
   * Returns the base name of the file, i.e., the file name without the extension.
   *
   * If the file doesn't have an extension, or is a hidden file (".hidden"), the
   * name is returned unchanged.
   */
  def basename(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot > 0) name.substring(0, dot) else name
  }

  def extension(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot > 0) name.substring(dot) else name
  }
}
