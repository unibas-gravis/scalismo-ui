package org.statismo.stk.ui.swing.actions

import scala.swing.Action
import scala.swing.FileChooser
import java.io.File
import scala.swing.Component
import javax.swing.filechooser.FileNameExtensionFilter
import org.statismo.stk.ui.StatModel
import org.statismo.stk.ui.swing.util.FileNameExtensionFilterWrapper
import org.statismo.stk.ui.Loadable
import org.statismo.stk.ui.SceneObject

class OpenStatisticalModelAction(val parent: Component, val onSelected: File => Unit, val name: String = "Open Statistical Model") extends Action(name) {
  val chooser = new FileChooser() {
    title = name
    fileFilter = new FileNameExtensionFilterWrapper().create("Statistical Model Files (.h5, .hdf5)", StatModel.fileExtensions.toArray);
  }
  def apply() = {
    if (chooser.showOpenDialog(parent) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      if (file != null) {
        onSelected(file)
      }
    }
  }
}

class OpenSceneObjectAction(val onSelected: (Seq[File], Seq[Loadable[SceneObject]]) => Unit, val name: String = "Open...", val factories: Seq[Loadable[SceneObject]] = Loadable.defaultFactories) extends Action(name) {
  val parentComponent: Component = null
  val allSupportedDescription = "All supported files"
  val chooser = new FileChooser() {
    title = name
    multiSelectionEnabled = true
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
      onSelected(chooser.selectedFiles, factories)
    }
  }
}