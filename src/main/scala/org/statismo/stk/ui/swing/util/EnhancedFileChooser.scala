package org.statismo.stk.ui.swing.util

import java.io.File
import javax.swing.JFileChooser
import java.awt.{Component => AComponent}
import javax.swing.JDialog
import javax.swing.JLabel
import java.awt.BorderLayout
import scala.swing.{ListView, Component, BorderPanel, Label}
import org.statismo.stk.ui.settings.PersistentSettings
import scala.util.Try

object EnhancedFileChooser {
  val LastUsedDirectoriesSettingsKey = "lastUsedDirectories"
}

class EnhancedFileChooser(dir:File)  extends scala.swing.FileChooser(dir) {
  import EnhancedFileChooser._
  override lazy val peer = new EnhancedJFileChooser
  def this() = this(null)

  // FIXME: Make configurable
  val MaxDirs = 5

  def getLastUsedDirectories : Seq[File] = {
    val dirNames = PersistentSettings.getList[String](LastUsedDirectoriesSettingsKey)
    if (dirNames.isSuccess) {
      dirNames.get.map(n => new File(n)).filter(d => d.isDirectory)
    }
    else Nil
  }

  def updateLastUsedDirectories(files: Seq[File]) = {
    val dirs = files.map(f => if (f.isDirectory) f else f.getParentFile).distinct
    if (!dirs.isEmpty) {
      val old = getLastUsedDirectories.diff(dirs)
      val current = Seq(dirs, old).flatten.take(MaxDirs)
      println(current)
    }
  }

  override def selectedFiles = {
    val r = super.selectedFiles
    updateLastUsedDirectories(r)
    r
  }

  override def selectedFile = {
    val r = super.selectedFile
    updateLastUsedDirectories(Seq(r))
    r
  }

  override def selectedFiles_=(files: File*) = {
    updateLastUsedDirectories(files)
    super.selectedFiles_=(files:_*)
  }

  override def selectedFile_=(file: File) = {
    updateLastUsedDirectories(Seq(file))
    super.selectedFile_=(file)
  }

  private [EnhancedFileChooser] class EnhancedJFileChooser extends JFileChooser {
    override def createDialog(parent: AComponent): JDialog = {
      val dialog = super.createDialog(parent)
      decorateDialog(dialog)
    }

    lazy val recentDirectories = getLastUsedDirectories

    def createRecentDirsPanel(lastDirs: Seq[File]): Component = {
      // FIXME
      val panel = new BorderPanel {
        val title = new Label("TODO")
        layout(title) = BorderPanel.Position.North

        val list = new ListView[String]
        list.listData= lastDirs.map(d => d.getName)
        layout(list) = BorderPanel.Position.Center
      }
      panel
    }


    lazy val leftComponent: Option[Component] = {
      if (recentDirectories.isEmpty) None
      else Some(createRecentDirsPanel(recentDirectories))
    }

    def decorateDialog(dialog: JDialog) = {
      val cp = dialog.getContentPane
      leftComponent match {
        case Some(component) => cp.add(component.peer, BorderLayout.WEST)
        case None =>
      }
      dialog
    }
  }

}
