package scalismo.ui.view.action

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter

import scalismo.ui.model.StatusMessage
import scalismo.ui.util.FileIoMetadata
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.ErrorDialog
import scalismo.ui.view.util.EnhancedFileChooser

import scala.swing.{ Action, FileChooser }
import scala.util.{ Failure, Success, Try }

object LoadAction {
  val DefaultName = "Load ..."
}

class LoadAction(val load: File => Try[Unit], val metadata: FileIoMetadata, val name: String = LoadAction.DefaultName, val multiSelect: Boolean = true)(implicit val frame: ScalismoFrame) extends Action(name) {
  lazy val chooserTitle = {
    if (name != LoadAction.DefaultName) name
    else "Load " + metadata.description
  }

  lazy val chooser = new EnhancedFileChooser() {
    title = chooserTitle
    multiSelectionEnabled = multiSelect
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new FileNameExtensionFilter(metadata.longDescription, metadata.fileExtensions: _*)
  }

  def parentComponent = frame.componentForDialogs

  def apply() = {
    if (chooser.showOpenDialog(parentComponent) == FileChooser.Result.Approve) {
      chooser.selectedFiles foreach tryLoad
    }
  }

  def tryLoad(file: File) = {
    val ok = load(file)
    ok match {
      case Success(_) => onSuccess(file)
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
