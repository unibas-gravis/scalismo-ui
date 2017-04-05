/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
