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

package scalismo.ui.view.action

import java.io.File

import javax.swing.filechooser.FileNameExtensionFilter
import scalismo.ui.model.StatusMessage
import scalismo.ui.util.FileIoMetadata
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.ErrorDialog
import scalismo.ui.view.util.EnhancedFileChooser

import scala.swing.{Action, Component, FileChooser}
import scala.util.{Failure, Success, Try}

object LoadAction {
  val DefaultName = "Load ..."
}

class LoadAction(val load: File => Try[Unit],
                 val metadata: FileIoMetadata,
                 val name: String = LoadAction.DefaultName,
                 val multiSelect: Boolean = true)(implicit val frame: ScalismoFrame)
    extends Action(name) {
  private lazy val chooserTitle = {
    if (name != LoadAction.DefaultName) name
    else "Load " + metadata.description
  }

  private lazy val chooser = new EnhancedFileChooser() {
    title = chooserTitle
    multiSelectionEnabled = multiSelect
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new FileNameExtensionFilter(metadata.longDescription, metadata.fileExtensions: _*)
  }

  def parentComponent: Component = frame.componentForDialogs

  def apply(): Unit = {
    if (chooser.showOpenDialog(parentComponent) == FileChooser.Result.Approve) {
      chooser.selectedFiles foreach tryLoad
    }
  }

  def tryLoad(file: File): Unit = {
    val ok = load(file)
    ok match {
      case Success(_)  => onSuccess(file)
      case Failure(ex) => onFailure(file, ex)
    }
  }

  def onSuccess(file: File): Unit = {
    frame.status.set(s"File loaded: ${file.getName}")
  }

  def onFailure(file: File, exception: Throwable): Unit = {
    val message = s"Unable to load file ${file.getName}"
    frame.status.set(StatusMessage(message, StatusMessage.Error))
    ErrorDialog.show(exception, additionalMessage = message, title = "Loading failed")
  }
}
