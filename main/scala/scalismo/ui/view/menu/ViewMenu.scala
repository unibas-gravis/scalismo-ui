package scalismo.ui.view.menu

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.HighDpiSetScaleAction

import scala.swing.event.Key
import scala.swing.{ Menu, MenuItem }

class ViewMenu extends Menu("View") {
  mnemonic = Key.V
}

object ViewMenu {

  class HighDpiSetScaleItem(implicit val frame: ScalismoFrame) extends MenuItem(new HighDpiSetScaleAction("Set UI scale")) {
    mnemonic = Key.S
  }

}

