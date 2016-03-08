package scalismo.ui.view.action

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.AboutDialog

import scala.swing.Action

class ShowAboutDialogAction(name: String = "About")(implicit frame: ScalismoFrame) extends Action(name) {
  override def apply(): Unit = {
    // the dialog takes care about showing itself correctly
    new AboutDialog()
  }

}
