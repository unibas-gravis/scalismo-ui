package scalismo.ui.settings

import java.io.File

import scala.util.{ Failure, Success, Try }

/**
 * Directory containing persistent settings, located in the user's home directory.
 */
object SettingsDirectory {
  private val Name = ".scalismo"

  // This only indicates if the root directory can be created at all, not necessarily if it exists.
  // This is safe to be a val, because it's solely based on user.home and Name, both of which don't change.
  private lazy val root: Option[File] = {
    Option(System.getProperty("user.home")).map { home =>
      new File(home + File.separator + SettingsDirectory.Name)
    }
  }

  def get(): Try[File] = {
    if (root.isEmpty) {
      Failure(new IllegalStateException("Unable to determine settings directory"))
    } else {
      getOrCreate(root.get)
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
