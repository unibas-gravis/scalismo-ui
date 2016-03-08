package scalismo.ui.view.menu

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.ShowAboutDialogAction

import scala.swing.event.Key
import scala.swing.{ Menu, MenuItem }

class HelpMenu extends Menu("Help") {
  mnemonic = Key.H
}

object HelpMenu {

  class ShowAboutDialogItem(implicit val frame: ScalismoFrame) extends MenuItem(new ShowAboutDialogAction) {
    mnemonic = Key.A
  }

}

