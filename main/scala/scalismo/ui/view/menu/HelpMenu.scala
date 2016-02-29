package scalismo.ui.view.menu

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.AboutDialogAction

import scala.swing.event.Key
import scala.swing.{ Menu, MenuItem }

class HelpMenu extends Menu("Help") {
  mnemonic = Key.H
}

object HelpMenu {

  class AboutItem(implicit val frame: ScalismoFrame) extends MenuItem(new AboutDialogAction("About")) {
    mnemonic = Key.A
  }

}

