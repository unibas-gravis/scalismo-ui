package org.statismo.stk.ui.swing.util

import java.io.File
import javax.swing.{BorderFactory, JFileChooser, JDialog}
import java.awt.{Component => AComponent, Color, Point, BorderLayout}
import scala.swing.{ListView, Component, BorderPanel, Label}
import org.statismo.stk.ui.settings.PersistentSettings
import java.awt.event.{MouseAdapter, MouseEvent, MouseMotionAdapter}
import scala.util.Failure

object EnhancedFileChooser {
  val LastUsedDirectoriesSettingsKey = "common.lastUsedDirectories"
}

class EnhancedFileChooser(dir:File)  extends scala.swing.FileChooser(dir) {
  import EnhancedFileChooser._
  override lazy val peer = new EnhancedJFileChooser
  def this() = this(null)

  val MaxDirs = 13

  def lastUsedDirectories : Seq[File] = {
    val dirNames = PersistentSettings.getList[String](LastUsedDirectoriesSettingsKey, Some(Nil))
    if (dirNames.isSuccess) {
      dirNames.get.map(n => new File(n)).filter(d => d.isDirectory).take(MaxDirs)
    }
    else {
      dirNames.failed.get.printStackTrace()
      Nil
    }
  }

  def lastUsedDirectories_=(files: Seq[File]) = {
    val dirs = files.map(f => if (f.isDirectory) f else f.getParentFile).distinct
    if (!dirs.isEmpty) {
      val old = lastUsedDirectories.diff(dirs)
      val current = Seq(dirs, old).flatten.take(MaxDirs)
      PersistentSettings.setList[String](LastUsedDirectoriesSettingsKey, current.map(_.getAbsolutePath).toList) match {
        case Failure(x) => x.printStackTrace()
        case _ => /* ok */
      }
    }
  }

  override def selectedFiles = {
    val r = super.selectedFiles
    lastUsedDirectories = r
    r
  }

  override def selectedFile = {
    val r = super.selectedFile
    lastUsedDirectories = Seq(r)
    r
  }

  override def selectedFiles_=(files: File*) = {
    lastUsedDirectories = files
    super.selectedFiles_=(files:_*)
  }

  override def selectedFile_=(file: File) = {
    lastUsedDirectories = Seq(file)
    super.selectedFile_=(file)
  }

  private [EnhancedFileChooser] class EnhancedJFileChooser extends JFileChooser {

    private [EnhancedJFileChooser] class FileEntry(val dir: File) {
      override def toString = dir.getName
      def tooltip = dir.getCanonicalPath
    }

    override def createDialog(parent: AComponent): JDialog = {
      val dialog = super.createDialog(parent)
      decorateDialog(dialog)
    }

    def createRecentDirsPanel(lastDirs: Seq[File]): Component = {

      val panel = new BorderPanel {
        val title = new BorderPanel{layout(new Label("Recent Folders:")) = BorderPanel.Position.West}
        layout(title) = BorderPanel.Position.North

        val list = new ListView[FileEntry] {
          def affectedItem(point: Point): Option[FileEntry] = {
            val m = peer.getModel
            val index = peer.locationToIndex(point)
            if (index > -1 && peer.getCellBounds(index,index).contains(point)) {
              Some(m.getElementAt(index).asInstanceOf[FileEntry])
            } else {
              None
            }
          }

          peer.addMouseMotionListener(new MouseMotionAdapter {
            override def mouseMoved(e: MouseEvent) = {
              affectedItem(e.getPoint) match {
                case None => peer.setToolTipText(null)
                case Some(f) => peer.setToolTipText(f.tooltip)
              }
            }
          })

          peer.addMouseListener(new MouseAdapter{
            override def mouseClicked(e: MouseEvent) = {
              if (e.getClickCount >= 2 && e.getButton == MouseEvent.BUTTON1) {
                affectedItem(e.getPoint).map(f => EnhancedJFileChooser.this.setCurrentDirectory(f.dir))
              }
            }
          })
        }

        list.listData= lastDirs.map(d => new FileEntry(d))
        list.border = BorderFactory.createLineBorder(Color.GRAY, 1)
        layout(list) = BorderPanel.Position.Center
      }
      panel.border = BorderFactory.createEmptyBorder(17,10,12,8)
      panel
    }


    lazy val leftComponent: Option[Component] = {
      val l = lastUsedDirectories
      if (l.isEmpty) None
      else Some(createRecentDirsPanel(l))
    }

    def decorateDialog(dialog: JDialog) = {
      val cp = dialog.getContentPane
      leftComponent match {
        case Some(c) => cp.add(c.peer, BorderLayout.WEST)
        case None =>
      }
      dialog
    }
  }

}
