package scalismo.ui.swing.menu

import scalismo.ui.swing.actions.AboutAction

import scala.swing.{ Menu, MenuItem }

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
