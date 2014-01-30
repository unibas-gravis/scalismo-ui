package org.statismo.stk.ui.swing.menu

import scala.swing.Menu
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.QuitAction
import org.statismo.stk.ui.StatismoFrame

object FileMenu {
  val Name = "File"
}

class FileMenu(implicit app: StatismoFrame) extends Menu(FileMenu.Name) {
  contents += new QuitMenuItem(this)
}

object QuitMenuItem {
  val Name = "Quit"
}

class QuitMenuItem(parent: FileMenu)(implicit app: StatismoFrame) extends MenuItem(new QuitAction(QuitMenuItem.Name, app))