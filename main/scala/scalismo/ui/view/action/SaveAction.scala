package scalismo.ui.view.action

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter

import scalismo.ui.model.StatusMessage
import scalismo.ui.util.FileIoMetadata
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.ErrorDialog
import scalismo.ui.view.util.EnhancedFileChooser

import scala.swing.{ Action, Dialog, FileChooser }
import scala.util.{ Failure, Success, Try }

object SaveAction {
  val DefaultName = "Save ..."
}

class SaveAction(val save: File => Try[Unit], val metadata: FileIoMetadata, val name: String = SaveAction.DefaultName)(implicit val frame: ScalismoFrame) extends Action(name) {
  lazy val confirmWhenExists = true
  lazy val verifyFileExtension = true
  lazy val chooserTitle = {
    if (name != SaveAction.DefaultName) name
    else "Save " + metadata.description
  }

  lazy val chooser = new EnhancedFileChooser() {
    title = chooserTitle
    multiSelectionEnabled = false
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new FileNameExtensionFilter(metadata.longDescription, metadata.fileExtensions: _*)
  }

  def parentComponent = frame.componentForDialogs

  def apply() = {
    if (chooser.showSaveDialog(parentComponent) == FileChooser.Result.Approve) {
      if (chooser.selectedFile.exists && confirmWhenExists) {
        val result = Dialog.showConfirmation(parentComponent, "The file " + chooser.selectedFile.getName + " already exists.\nDo you want to overwrite it?", "Overwrite existing file?", Dialog.Options.OkCancel)
        result match {
          case Dialog.Result.Ok => verifyThenSave(chooser.selectedFile)
          case _ =>
        }
      } else verifyThenSave(chooser.selectedFile)
    }
  }

  def verifyThenSave(file: File) = {
    def candidateName = file.getName.toLowerCase
    var verified = true
    if (verifyFileExtension) {
      val matching = metadata.fileExtensions.filter {
        ext => candidateName.endsWith("." + ext.toLowerCase)
      }
      if (matching.isEmpty) {
        val msg = s"The file name that you provided (${file.getName}) seems to have an unsupported file extension.\nDo you still wish to create the file?"
        val result = Dialog.showConfirmation(parentComponent, msg, "Create file with unsupported extension?", Dialog.Options.OkCancel)
        verified = result == Dialog.Result.Ok
      }
    }
    if (verified) trySave(file)
  }

  def trySave(file: File) = {
    val ok = save(file)
    ok match {
      case Success(_) => onSuccess(file)
      case Failure(ex) => onFailure(file, ex)
    }
  }

  def onSuccess(file: File): Unit = {
    frame.status.set(s"File saved: ${file.getName}")
  }

  def onFailure(file: File, exception: Throwable): Unit = {
    val message = s"Unable to save file ${file.getName}"
    frame.status.set(StatusMessage(message, StatusMessage.Error))
    ErrorDialog.show(exception, additionalMessage = message, title = "Saving failed")
  }

}

