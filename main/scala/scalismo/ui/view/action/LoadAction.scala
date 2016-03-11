package scalismo.ui.view.action

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter

import scalismo.ui.util.FileIoMetadata
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.EnhancedFileChooser

import scala.swing.{ Action, Dialog, FileChooser }
import scala.util.{ Failure, Success, Try }

object LoadAction {
  val DefaultName = "Load..."
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

  def parentComponent = frame.contents.head

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
    frame.statusBar.set(s"File loaded: ${file.getName}")
  }

  def onFailure(file: File, exception: Throwable): Unit = {
    Dialog.showMessage(parentComponent, "ERROR:\n" + exception.getMessage, "Load failed", Dialog.Message.Error)
  }
}
