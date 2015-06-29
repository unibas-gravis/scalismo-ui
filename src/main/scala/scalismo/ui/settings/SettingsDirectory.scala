package scalismo.ui.settings

import java.io.File

import scala.util.{ Failure, Success, Try }

object SettingsDirectory {
  private val Name = ".scalismo"

  // this only indicates if the root directory can be created at all, not necessarily if it exists.
  private lazy val root: Option[File] = {
    val home = System.getProperty("user.home")
    if (home != null) {
      Some(new File(home + File.separator + SettingsDirectory.Name))
    } else {
      None
    }
  }

  def get(allowInexistent: Boolean = true): Try[File] = {
    if (!root.isDefined) {
      Failure(new IllegalStateException("Unable to determine settings directory"))
    } else {
      val dir = root.get
      if (allowInexistent) Success(dir)
      else getOrCreate(dir)
    }
  }

  private def getOrCreate(dir: File): Try[File] = {
    if (dir.exists()) {
      if (dir.isDirectory) Success(dir)
      else Failure(new IllegalStateException(dir + " was expected to be a directory, but is a file"))
    } else {
      if (!dir.mkdirs()) {
        Failure(new RuntimeException("Failed to create directory hierarchy"))
      }
      Success(dir)
    }
  }
}
