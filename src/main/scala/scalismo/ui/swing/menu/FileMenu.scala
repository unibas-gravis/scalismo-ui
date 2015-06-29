package scalismo.ui.swing.menu

import scalismo.ui.swing.ScalismoFrame
import scalismo.ui.swing.actions.QuitAction

import scala.swing.{ Menu, MenuItem }

object FileMenu {
  val Name = "File"
}

class FileMenu(implicit app: ScalismoFrame) extends Menu(FileMenu.Name) {
  contents += new QuitMenuItem(this)
}

object QuitMenuItem {
  val Name = "Quit"
}

class QuitMenuItem(parent: FileMenu)(implicit app: ScalismoFrame) extends MenuItem(new QuitAction(QuitMenuItem.Name, app))