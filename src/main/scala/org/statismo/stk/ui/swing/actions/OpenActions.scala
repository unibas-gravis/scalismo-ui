package org.statismo.stk.ui.swing.actions

import java.io.File

import scala.swing.Action
import scala.swing.Component
import scala.swing.FileChooser

import org.statismo.stk.ui.Loadable
import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.swing.util.FileNameExtensionFilterWrapper

import javax.swing.filechooser.FileNameExtensionFilter

class OpenSceneTreeObjectAction(val onSelected: (Seq[File], Seq[Loadable[SceneTreeObject]]) => Unit, val name: String = "Open...", val factories: Seq[Loadable[SceneTreeObject]] = Loadable.DefaultFactories, val multipleSelection: Boolean = true) extends Action(name) {
  val parentComponent: Component = null
  val allSupportedDescription = "All supported files"
  val chooser = new FileChooser() {
    title = name
    multiSelectionEnabled = multipleSelection
    peer.setAcceptAllFileFilterUsed(false)
    val combinedFilter: Option[FileNameExtensionFilter] = {
      if (factories.size <= 1) None else {
        Some(new FileNameExtensionFilterWrapper().create(allSupportedDescription, Loadable.combineFileExtensions(factories)))
      }
    }
    val fnfilters = factories.map(f => new FileNameExtensionFilterWrapper().create(f.longDescription, f.fileExtensions.toArray))
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