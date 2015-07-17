package scalismo.ui.swing.menu

import scalismo.ui.swing.ScalismoFrame
import scalismo.ui.swing.actions.ExitAction

import scala.swing.{ Menu, MenuItem }

object FileMenu {
  val Name = "File"
}

class FileMenu(implicit app: ScalismoFrame) extends Menu(FileMenu.Name) {
  contents += new ExitMenuItem(this)
}

object ExitMenuItem {
  val Name = "Exit"
}

class ExitMenuItem(parent: FileMenu)(implicit app: ScalismoFrame) extends MenuItem(new ExitAction(ExitMenuItem.Name, app))
