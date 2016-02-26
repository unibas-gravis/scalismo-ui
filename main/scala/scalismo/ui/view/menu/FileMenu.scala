package scalismo.ui.view.menu

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.ExitAction

import scala.swing.event.Key
import scala.swing.{Menu, MenuItem}

class FileMenu extends Menu("File") {
  mnemonic = Key.F
}

object FileMenu {

  class ExitItem(implicit frame: ScalismoFrame) extends MenuItem(new ExitAction("Exit")) {
    mnemonic = Key.X
  }

}

