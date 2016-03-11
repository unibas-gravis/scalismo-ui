package scalismo.ui.view.util

import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionAdapter }
import java.awt.{ BorderLayout, Color, Point, Component => AComponent }
import java.io.File
import javax.swing._

import scalismo.ui.settings.GlobalSettings

import scala.swing.{ FileChooser, Label, BorderPanel, Component }
import scala.util.Failure
import ScalableUI.implicits.scalableInt

object EnhancedFileChooser {
  var MaxDirs = 13
}

/*
 * This still looks slightly ugly with the scalable UI, but that's only a cosmetic issue.
 */
class EnhancedFileChooser(dir: File) extends FileChooser(dir) {

  override lazy val peer = new EnhancedJFileChooser

  def this() = this(null)

  def lastUsedDirectories: Seq[File] = {
    val dirNames = GlobalSettings.getList[String](GlobalSettings.Keys.LastUsedDirectories)
    if (dirNames.isDefined) {
      dirNames.get.map(n => new File(n)).filter(d => d.isDirectory).take(EnhancedFileChooser.MaxDirs)
    } else {
      Nil
    }
  }

  def lastUsedDirectories_=(files: Seq[File]) = {
    val dirs = files.map(f => if (f.isDirectory) f else f.getParentFile).distinct
    if (dirs.nonEmpty) {
      val old = lastUsedDirectories.diff(dirs)
      val current = Seq(dirs, old).flatten.take(EnhancedFileChooser.MaxDirs)
      GlobalSettings.setList[String](GlobalSettings.Keys.LastUsedDirectories, current.map(_.getAbsolutePath).toList) match {
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
    super.selectedFiles_=(files: _*)
  }

  override def selectedFile_=(file: File) = {
    lastUsedDirectories = Seq(file)
    super.selectedFile_=(file)
  }

  private[EnhancedFileChooser] class EnhancedJFileChooser extends JFileChooser {

    private[EnhancedJFileChooser] class FileEntry(val dir: File) {
      override def toString = dir.getName

      def tooltip = dir.getCanonicalPath
    }

    override def createDialog(parent: AComponent): JDialog = {
      val dialog = super.createDialog(parent)
      decorateDialog(dialog)
    }

    def createRecentDirsPanel(lastDirs: Seq[File]): Component = {

      val model = new DefaultListModel[FileEntry]()
      lastDirs.foreach(d => model.addElement(new FileEntry(d)))

      val panel = new BorderPanel {
        val title = new BorderPanel {
          layout(new Label("Recent Folders:")) = BorderPanel.Position.West
        }
        layout(title) = BorderPanel.Position.North

        val list = new JList(model) {
          def affectedItem(point: Point): Option[FileEntry] = {
            val index = locationToIndex(point)
            if (index > -1 && getCellBounds(index, index).contains(point)) {
              Some(model.getElementAt(index))
            } else {
              None
            }
          }

          addMouseMotionListener(new MouseMotionAdapter {
            override def mouseMoved(e: MouseEvent) = {
              affectedItem(e.getPoint) match {
                case None => setToolTipText(null)
                case Some(f) => setToolTipText(f.tooltip)
              }
            }
          })

          addMouseListener(new MouseAdapter {
            override def mouseClicked(e: MouseEvent) = {
              if (e.getClickCount >= 2 && e.getButton == MouseEvent.BUTTON1) {
                affectedItem(e.getPoint).foreach(f => EnhancedJFileChooser.this.setCurrentDirectory(f.dir))
              }
            }
          })
        }

        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1.scaled))
        layout(Component.wrap(list)) = BorderPanel.Position.Center
      }
      panel.border = BorderFactory.createEmptyBorder(17.scaled, 10.scaled, 12.scaled, 8.scaled)
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
      if (dialog.getPreferredSize.getWidth > dialog.getSize.getWidth) {
        dialog.setSize(dialog.getPreferredSize)
      }
      dialog
    }
  }

}
