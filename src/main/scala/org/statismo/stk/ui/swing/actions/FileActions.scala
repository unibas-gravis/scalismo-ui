package org.statismo.stk.ui.swing.actions

import java.io.File

import scala.swing.Action
import scala.swing.Component
import scala.swing.Dialog
import scala.swing.FileChooser
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.statismo.stk.ui.FileIoMetadata
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.SceneTreeObjectFactory
import org.statismo.stk.ui.swing.util.FileNameExtensionFilterWrapper

import javax.swing.filechooser.FileNameExtensionFilter

class OpenSceneTreeObjectAction(val onSelected: (Seq[File], Seq[SceneTreeObjectFactory[SceneTreeObject]]) => Unit, val name: String = "Open...", val factories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = SceneTreeObjectFactory.DefaultFactories, val multipleSelection: Boolean = true) extends Action(name) {
  val parentComponent: Component = null
  val allSupportedDescription = "All supported files"
  val chooser = new FileChooser() {
    title = name
    multiSelectionEnabled = multipleSelection
    peer.setAcceptAllFileFilterUsed(false)
    val combinedFilter: Option[FileNameExtensionFilter] = {
      if (factories.size <= 1) None else {
        Some(new FileNameExtensionFilterWrapper().create(allSupportedDescription, SceneTreeObjectFactory.combineFileExtensions(factories)))
      }
    }
    val fnfilters = factories.map(f => new FileNameExtensionFilterWrapper().create(f.ioMetadata.longDescription, f.ioMetadata.fileExtensions.toArray))
    fileFilter = combinedFilter.getOrElse(fnfilters.head)
    fnfilters.drop(if (combinedFilter.isDefined) 0 else 1).foreach(peer.addChoosableFileFilter(_))
  }
  def apply() = {
    if (chooser.showOpenDialog(parentComponent) == FileChooser.Result.Approve) {
      if (chooser.multiSelectionEnabled) {
        onSelected(chooser.selectedFiles, factories)
      } else {
        onSelected(Seq(chooser.selectedFile), factories)
      }
    }
  }
}

object SaveAction {
  val DefaultName = "Save..."
}

class SaveAction(val save: File => Try[Unit], val metadata: FileIoMetadata, val name: String = SaveAction.DefaultName) extends Action(name) {
  lazy val confirmWhenExists = true
  lazy val verifyFileExtension = true
  lazy val chooserTitle = {
    if (name != SaveAction.DefaultName) name
    else "Save " + metadata.description
  }
  lazy val parentComponent: Component = null

  lazy val chooser = new FileChooser() {
    title = chooserTitle
    multiSelectionEnabled = false
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new FileNameExtensionFilterWrapper().create(metadata.longDescription, metadata.fileExtensions.toArray)
  }

  def apply() = {
    if (chooser.showSaveDialog(parentComponent) == FileChooser.Result.Approve) {
      if (chooser.selectedFile.exists && confirmWhenExists) {
        val result = Dialog.showConfirmation(parentComponent, "The file " + chooser.selectedFile.getName() + " already exists.\nDo you want to overwrite it?", "Overwrite existing file?", Dialog.Options.OkCancel)
        result match {
          case Dialog.Result.Ok => verifyThenSave(chooser.selectedFile)
          case _ => {}
        }
      } else verifyThenSave(chooser.selectedFile)
    }
  }

  def verifyThenSave(file: File) = {
    def candidateName = file.getName().toLowerCase()
    var verified = true
    if (verifyFileExtension) {
      val matching = metadata.fileExtensions.filter { ext => candidateName.endsWith("." + ext.toLowerCase()) }
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

  def onSuccess(file: File) {
    Dialog.showMessage(parentComponent, "Successfully saved: " + file.getName(), "File saved")
  }

  def onFailure(file: File, exception: Throwable) {
    Dialog.showMessage(parentComponent, "ERROR:\n" + exception.getMessage(), "Save failed", Dialog.Message.Error)
  }
}

object LoadAction {
  val DefaultName = "Load..."
}

class LoadAction(val load: File => Try[Unit], val metadata: FileIoMetadata, val name: String = LoadAction.DefaultName) extends Action(name) {
  lazy val chooserTitle = {
    if (name != LoadAction.DefaultName) name
    else "Load " + metadata.description
  }
  lazy val parentComponent: Component = null

  lazy val chooser = new FileChooser() {
    title = chooserTitle
    multiSelectionEnabled = false
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new FileNameExtensionFilterWrapper().create(metadata.longDescription, metadata.fileExtensions.toArray)
  }

  def apply() = {
    if (chooser.showOpenDialog(parentComponent) == FileChooser.Result.Approve) {
      tryLoad(chooser.selectedFile)
    }
  }

  def tryLoad(file: File) = {
    val ok = load(file)
    ok match {
      case Success(_) => onSuccess(file)
      case Failure(ex) => onFailure(file, ex)
    }
  }

  def onSuccess(file: File) {
  }

  def onFailure(file: File, exception: Throwable) {
    exception.printStackTrace()
    Dialog.showMessage(parentComponent, "ERROR:\n" + exception.getMessage(), "Load failed", Dialog.Message.Error)
  }
}