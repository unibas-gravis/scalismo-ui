package scalismo.ui.swing.menu

import scalismo.ui.swing.ScalismoFrame
import scalismo.ui.swing.actions.{ ShowAboutDialogAction }

import scala.swing.event.Key
import scala.swing.{ Menu, MenuItem }

object HelpMenu {
  val Name = "Help"
}

class HelpMenu(frame: ScalismoFrame) extends Menu(HelpMenu.Name) {
  contents += new AboutMenuItem(frame)
}

object AboutMenuItem {
  val Name = "About"
}

//class AboutMenuItem extends MenuItem(new AboutAction(AboutMenuItem.Name))

class AboutMenuItem(frame: ScalismoFrame) extends MenuItem(new ShowAboutDialogAction()(frame)) {
  implicit val f = frame
  mnemonic = Key.A
}