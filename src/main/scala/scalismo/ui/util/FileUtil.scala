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

  /**
   * Returns the extension of the file, i.e., the portion of the file name after the last dot.
   * If the file is a hidden file (starts with a single dot) without an extension (e.g., ".csv"), or if the file doesn't contain a dot
   * at all, then an empty String ("") is returned.
   */
  def extension(file: File): String = {
    val name = file.getName
    val dot = name.lastIndexOf('.')
    if (dot > 0) name.substring(dot + 1) else ""
  }
}
