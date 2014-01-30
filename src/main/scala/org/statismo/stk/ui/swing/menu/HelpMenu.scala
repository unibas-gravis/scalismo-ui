package org.statismo.stk.ui.swing.menu

import scala.swing.Menu
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.AboutAction

object HelpMenu {
  val Name = "Help"
}

class HelpMenu extends Menu(HelpMenu.Name) {
  contents += new AboutMenuItem
}

object AboutMenuItem {
  val Name = "About"
}

class AboutMenuItem extends MenuItem(new AboutAction(AboutMenuItem.Name))