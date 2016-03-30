package scalismo.ui.view.menu

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.CloseFrameAction

import scala.swing.event.Key
import scala.swing.{ Menu, MenuItem }

class FileMenu extends Menu("File") {
  mnemonic = Key.F
}

object FileMenu {

  class CloseFrameItem(implicit frame: ScalismoFrame) extends MenuItem(new CloseFrameAction) {
    mnemonic = Key.C
  }

}

